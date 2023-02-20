package io.agora.metachat.tools

import io.agora.base.VideoFrame
import io.agora.rtc2.video.AgoraVideoFrame

/**
 * @author create by zhangwei03
 *
 */
object MediaTools {
    fun transformVideoFrame(frame: VideoFrame): AgoraVideoFrame {
        val result = AgoraVideoFrame()
        result.format = AgoraVideoFrame.FORMAT_I420
        result.timeStamp = frame.timestampNs
        result.rotation = frame.rotation
        val i420buffer = frame.buffer.toI420()
        result.stride = i420buffer.strideY
        result.height = i420buffer.height
        result.buf = getByteArray(i420buffer)
        i420buffer.release()
        return result
    }

    private fun getByteArray(buffer: VideoFrame.I420Buffer): ByteArray {
        val lengthY = buffer.strideY * buffer.height
        val lengthU = lengthY / 4
        val result = ByteArray(lengthY * 3 / 2)

        var offset = 0
        buffer.dataY.position(0)
        buffer.dataY.get(result, offset, lengthY)
        offset += lengthY
        buffer.dataU.position(0)
        buffer.dataU.get(result, offset, lengthU)
        offset += lengthU
        buffer.dataV.position(0)
        buffer.dataV.get(result, offset, lengthU)
        return result
    }
}