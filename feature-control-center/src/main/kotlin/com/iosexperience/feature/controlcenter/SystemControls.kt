package com.iosexperience.feature.controlcenter

import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.roundToInt

/** Estado REAL lido do sistema. null = não foi possível determinar. */
data class ControlState(
    val wifi: Boolean?, val bluetooth: Boolean?, val airplane: Boolean, val autoRotate: Boolean,
    val dnd: Boolean, val torch: Boolean, val brightness: Float, val volume: Float,
    val canWriteSettings: Boolean, val dndAccess: Boolean,
)

/** Resultado de uma ação: nunca finge sucesso. */
sealed interface ActionResult {
    data object Done : ActionResult
    /** O app não pode fazer isso sozinho: abriu a tela de configurações correspondente. */
    data class Redirected(val message: String) : ActionResult
    data class Failed(val message: String) : ActionResult
}

class SystemControls private constructor(private val ctx: Context) {
    private val cm = ctx.getSystemService(CameraManager::class.java)
    private val am = ctx.getSystemService(AudioManager::class.java)
    private val nm = ctx.getSystemService(NotificationManager::class.java)
    val torchOn = MutableStateFlow(false)
    private val torchId: String? = try {
        cm.cameraIdList.firstOrNull {
            val c = cm.getCameraCharacteristics(it)
            c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true && c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }
    } catch (e: Exception) { null }

    init {
        try { cm.registerTorchCallback(object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) { if (cameraId == torchId) torchOn.value = enabled }
        }, null) } catch (e: Exception) { }
    }

    private fun open(intent: Intent, msg: String): ActionResult = try {
        ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); ActionResult.Redirected(msg)
    } catch (e: Exception) { ActionResult.Failed("Não foi possível abrir as configurações") }

    fun snapshot(): ControlState {
        val resolver = ctx.contentResolver
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        return ControlState(
            wifi = try { ctx.applicationContext.getSystemService(WifiManager::class.java)?.isWifiEnabled } catch (e: Exception) { null },
            bluetooth = try { ctx.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled } catch (e: SecurityException) { null },
            airplane = Settings.Global.getInt(resolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1,
            autoRotate = Settings.System.getInt(resolver, Settings.System.ACCELEROMETER_ROTATION, 1) == 1,
            dnd = nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL && nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN,
            torch = torchOn.value,
            brightness = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 128) / BRIGHTNESS_MAX,
            volume = am.getStreamVolume(AudioManager.STREAM_MUSIC) / max.toFloat(),
            canWriteSettings = Settings.System.canWrite(ctx),
            dndAccess = nm.isNotificationPolicyAccessGranted,
        )
    }

    fun toggleTorch(): ActionResult {
        val id = torchId ?: return ActionResult.Failed("Este aparelho não expõe a lanterna")
        return try { cm.setTorchMode(id, !torchOn.value); ActionResult.Done } catch (e: Exception) { ActionResult.Failed("Lanterna indisponível agora (câmera em uso?)") }
    }

    fun setVolume(v: Float) { val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); am.setStreamVolume(AudioManager.STREAM_MUSIC, (v * max).roundToInt().coerceIn(0, max), 0) }

    fun writeSettingsIntent() = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${ctx.packageName}"))

    /** Exige WRITE_SETTINGS (concedida pelo usuário). No HyperOS o intervalo real pode ser maior que 255 — validar no aparelho. */
    fun setBrightness(v: Float): ActionResult {
        if (!Settings.System.canWrite(ctx)) return open(writeSettingsIntent(), "Conceda “Modificar configurações do sistema” para controlar o brilho")
        return try { Settings.System.putInt(ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS, (v * BRIGHTNESS_MAX).roundToInt().coerceIn(1, BRIGHTNESS_MAX.toInt())); ActionResult.Done }
        catch (e: Exception) { ActionResult.Failed("O sistema recusou a alteração de brilho") }
    }

    fun toggleAutoRotate(current: Boolean): ActionResult {
        if (!Settings.System.canWrite(ctx)) return open(writeSettingsIntent(), "Conceda “Modificar configurações do sistema” para girar automaticamente")
        return try { Settings.System.putInt(ctx.contentResolver, Settings.System.ACCELEROMETER_ROTATION, if (current) 0 else 1); ActionResult.Done }
        catch (e: Exception) { ActionResult.Failed("O sistema recusou a alteração") }
    }

    fun toggleDnd(current: Boolean): ActionResult {
        if (!nm.isNotificationPolicyAccessGranted) return open(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), "Conceda “Acesso ao Não perturbe” para controlar este modo")
        nm.setInterruptionFilter(if (current) NotificationManager.INTERRUPTION_FILTER_ALL else NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        return ActionResult.Done
    }

    // Apps comuns não podem ligar/desligar estes: abrimos o painel oficial.
    fun openWifi() = open(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY), "O Android não permite que apps alternem o Wi-Fi: use o painel")
    fun openBluetooth() = open(Intent(Settings.ACTION_BLUETOOTH_SETTINGS), "O Android não permite que apps alternem o Bluetooth: abrindo configurações")
    fun openAirplane() = open(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS), "O Android não permite que apps alternem o modo avião: abrindo configurações")

    companion object {
        const val BRIGHTNESS_MAX = 255f
        @Volatile private var inst: SystemControls? = null
        fun get(ctx: Context) = inst ?: synchronized(this) { inst ?: SystemControls(ctx.applicationContext).also { inst = it } }
    }
}
