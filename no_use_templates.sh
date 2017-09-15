#!/bin/bash
# a simple utilty to find backbone templates not used
path=web/src/main/webapp/js/
file1=ctd2.js
file2=template.helper.js

# extract all template names from index.jsp
template_names=`grep "<script type=\"text/template\"" web/src/main/webapp/index.jsp | grep -oP 'id=\"\K[a-zA-Z-]*(?=\")'`
for tn in $template_names; do
	if ! grep $tn $path$file2 > /dev/null && ! grep $tn $path$file1 > /dev/null; then echo $tn; fi
done
