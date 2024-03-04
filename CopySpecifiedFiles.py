import os
import shutil

oldDir = '/Users/tanjiajun/lame-3.100/libmp3lame'
newDir = '/Users/tanjiajun/StudioProjects/AndroidAudioDemo/app/src/main/cpp/lame'
cExtension = '.c'
hExtension = '.h'

if not os.path.exists(newDir):
    os.makedirs(newDir)
for file in os.listdir(oldDir):
    extension = os.path.splitext(file)[-1]
    if extension == cExtension or extension == hExtension:
        shutil.copy(os.path.join(oldDir, file), newDir)
        print(os.path.join(newDir, file))