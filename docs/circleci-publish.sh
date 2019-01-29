#!/bin/bash



SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $SCRIPT_DIR

SOURCE_SHA1=${CIRCLE_SHA1-"unknown"}
rm -rf ./tmp-clone ./site

DOCS_DIGEST=$(find . -type f -print0 | xargs -0 sha1sum | sha1sum | awk '{ print $1 }')


REMOTE_URL=git@github.com:rebar-cloud/rebar-cloud.git

git clone $REMOTE_URL tmp-clone || exit 99

EXISTING_DIGEST=$(head -1 ./tmp-clone/digest.txt)

cat ./tmp-clone/digest.txt
echo $DOCS_DIGEST

if [ "$DOCS_DIGEST" = "$EXISTING_DIGEST" ]; then
echo 
echo docs have not changed
exit 0
fi

exit 1



sudo apt-get install python-pip
sudo pip install mkdocs pygments mkdocs-material

echo $DOCS_DIGEST >docs/digest.txt

mkdocs build || exit 99

cp -r site/* tmp-clone || exit 99

cd tmp-clone

  git config --global user.email "robschoening@gmail.com"
  git config --global user.name "Circle CI"

git add .
git commit -a -m "docs built from $SOURCE_SHA1" 

git push || exit 99


