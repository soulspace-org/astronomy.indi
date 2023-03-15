astronomy.indi
==============
The astronomy.indi library contains an implementation of the [INDI protocol](http://www.clearskyinstitute.com/INDI/INDI.pdf)
for the control of astronomical instruments.
The INDI protocol is used to connect and control astronomical instruments with software clients like planetarium software.
It specifies the communication between devices and clients. The (physical) devices are represented and handled with drivers. Servers can be used as intermediaries between drivers and clients to decouple them.

indi4clj provides the implementation of the XML based INDI protocol and abstractions for creating INDI drivers, servers and clients. 

indi4clj is not a wrapper for a Java or C implementation of the INDI protocol like [INDIForJava](https://www.indilib.org/develop/indiforjava.html) or
[INDIlib](https://www.indilib.org/).
It is written from scratch because I believe a native implementation in Clojure is less complex and therefore much easier to understand, maintain and extend than an implementation in Java or C.

Usage
-----
Leiningen dependency

```
[org.soulspace.clj/astronomy.indi "0.1.0-SNAPSHOT"]
```

## Copyright
Copyright © 2020-2021 Ludger Solbach

## License

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
