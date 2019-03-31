## This script automates deploying of update.zip to puruscor.github.io, unzips it, commits and pushes
unzip -o build/update.zip -d ../puruscor.github.io/HnH/
cd ../puruscor.github.io/
git add ./HnH/*
git commit -m "Update client files"
git push
curl --request post https://api.github.com/repos/puruscor/puruscor.github.io/pages/builds
