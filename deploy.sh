## This script automates deploying of update.zip to puruscor.github.io, unzips it, commits and pushes
ant doc
rm -r ../purus-pasta-2-dist/public/javadoc
mv doc/javadoc ../purus-pasta-2-dist/public/
unzip -o build/update.zip -d ../purus-pasta-2-dist/public/HnH/
cd ../purus-pasta-2-dist/
git add ./*
git commit -m "Update client files"
sh ./update-hashes.sh
git add ./*
git commit -m "Update client file listing"
git push