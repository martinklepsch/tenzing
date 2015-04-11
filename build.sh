#!/bin/sh
rm -rf tenzing/
sass tenzing.sass tenzing.css
emacs README.org --batch -f org-html-export-to-html --kill
mv README.html index.html
cp bg-compressor.jpg bg.jpg
