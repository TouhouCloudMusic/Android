package net.hearnsoft.tcm.compose.pref

enum class PlayerSeekToPreviousAction(action: Int) {
    DEFAULT(0), // Media3 默认行为，即在播放位置大于 3 秒时，点击“上一个”按钮会将播放位置重置为 0 秒，否则切换到上一首歌曲
    ALWAYS_PREVIOUS(1), // 始终切换到上一首歌曲
    ALWAYS_RESTART(2) // 始终将播放位置重置为 0 秒
}