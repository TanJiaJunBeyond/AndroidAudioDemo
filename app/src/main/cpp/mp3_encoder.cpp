//
// Created by 谭嘉俊 on 2024/3/27.
//
#include <jni.h>
#include "lame/lame.h"

FILE *inputPCMFile = nullptr;
FILE *outputMP3File = nullptr;
lame_t lameClient = nullptr;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_tanjiajun_androidaudiodemo_utils_LAMEUtils_getLameVersion(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(get_lame_version());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_tanjiajun_androidaudiodemo_utils_LAMEUtils_init(
        JNIEnv *env,
        jobject thiz,
        jstring input_pcm_file_path,
        jstring output_mp3_file_path,
        jint sample_rate_in_hz,
        jint channel_count,
        jint bit_rate
) {
    // 将jstring类型的input_pcm_file_path转换为UTF-8编码的字符数组；因为不需要关心JVM是否会返回原始字符串的副本，所以isCopy参数传NULL
    const char *inputPCMFilePath = env->GetStringUTFChars(input_pcm_file_path, NULL);
    // 以读取二进制文件的方式打开需要输入的PCM文件，如果打开失败就返回NULL
    inputPCMFile = fopen(inputPCMFilePath, "rb");
    if (!inputPCMFile) {
        // 如果需要输入的PCM文件打开失败就返回false
        return false;
    }
    // 将jstring类型的output_mp3_file_path转换为UTF-8编码的字符数组；因为不需要关心JVM是否会返回原始字符串的副本，所以isCopy参数传NULL
    const char *outputMP3FilePath = env->GetStringUTFChars(output_mp3_file_path, NULL);
    // 以写入二进制文件的方式打开需要输出的MP3文件，如果打开失败就返回NULL
    outputMP3File = fopen(outputMP3FilePath, "wb");
    if (!outputMP3File) {
        // 如果需要输出的MP3文件打开失败就返回false
        return false;
    }
    // 初始化LAME编码器
    lameClient = lame_init();
    // 设置LAME编码器的输入采样率
    lame_set_in_samplerate(lameClient, sample_rate_in_hz);
    // 设置LAME编码器的输出采样率
    lame_set_out_samplerate(lameClient, sample_rate_in_hz);
    // 设置LAME编码器的量化格式
    lame_set_brate(lameClient, bit_rate / 1000);
    // 设置LAME编码器的声道数
    lame_set_num_channels(lameClient, channel_count);
    lame_init_params(lameClient);
    return true;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tanjiajun_androidaudiodemo_utils_LAMEUtils_encode(JNIEnv *env, jobject thiz) {
    if (!inputPCMFile || !outputMP3File || !lameClient) {
        return;
    }
    int bufferSize = 1024 * 256;
    short *buffer = new short[bufferSize / 2];
    short *leftBuffer = new short[bufferSize / 4];
    short *rightBuffer = new short[bufferSize / 4];
    unsigned char *mp3Buffer = new unsigned char[bufferSize];
    size_t readBufferSize;
    // 每次从PCM文件读取一段bufferSize大小的PCM数据buffer
    while ((readBufferSize = fread(buffer, 2, bufferSize / 2, inputPCMFile)) > 0) {
        for (int i = 0; i < readBufferSize; i++) {
            // 把该buffer的左右声道拆分开
            if (i % 2 == 0) {
                leftBuffer[i / 2] = buffer[i];
            } else {
                rightBuffer[i / 2] = buffer[i];
            }
        }
        // 编码左声道buffer和右声道buffer
        int wroteSize = lame_encode_buffer(
                lameClient,
                (short int *) leftBuffer,
                (short int *) rightBuffer,
                (int) (readBufferSize / 2),
                mp3Buffer,
                bufferSize
        );
        // 将编码后的数据写入MP3文件中
        fwrite(mp3Buffer, 1, wroteSize, outputMP3File);
    }
    // 释放内存，并且调用对象数组的析构函数
    delete[] buffer;
    delete[] leftBuffer;
    delete[] rightBuffer;
    delete[] mp3Buffer;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tanjiajun_androidaudiodemo_utils_LAMEUtils_destroy(JNIEnv *env, jobject thiz) {
    if (!inputPCMFile) {
        return;
    }
    // 关闭先前打开的PCM文件，把缓冲区内最后剩余的数据输出到内核缓冲区，并且释放文件指针和相关的缓冲区
    fclose(inputPCMFile);
    if (!outputMP3File) {
        return;
    }
    // 关闭先前打开的MP3文件，把缓冲区内最后剩余的数据输出到内核缓冲区，并且释放文件指针和相关的缓冲区
    fclose(outputMP3File);
    if (!lameClient) {
        return;
    }
    // 销毁LAME编码器
    lame_close(lameClient);
}