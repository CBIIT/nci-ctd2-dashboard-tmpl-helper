# NCI CTD<sup>2</sup> Dashboard Template Helper

## upload size limit

This appplication is pre-set to allow the maximal 10 MB upload (either as the attachment of a submission, or as the zipped package for validation).
If the file to be uploaded is larger than this size, you will see a warning message and no uploading will happen.

It is crucial to notice that the size of 10 MB is much bigger than the default maximum post size of Tomcat, which is 2097152 (2 megabytes).
So the setting **must** be added to the Tomcat. In the `conf/server.xml` file, you should find the element of `Connector`
and add a `maxPostSize` attribute as in the following example.

```xml
<Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000" maxPostSize="20000000"
               redirectPort="8443" />
```

Considering the encoding-caused widening and addtional data uploaded,
it is a sensible to set attribute `maxPostSize` to be double the size allowed for the uploaded file.
If the uploading size is too big (or `maxPostSize` is too small),
you will *not* see any warning from Tomcat but the subsequent behavior is likely to be unpredictable.