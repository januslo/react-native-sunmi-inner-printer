# react-native-sunmi-inner-printer
http://docs.sunmi.com/htmls/index.html?lang=zh##V1文档资源  根据商米V1文档开发打印接口
(React native plugin Referring the sunmi V1 printer document and demos)


_ **Caution: this is not the official project. I share it because I am working on this device but no any official support in react-native It's welcome to ask any question about the usage,problems or feature required, I will support ASAP.**_

for scanner, refer this: https://github.com/januslo/react-native-sunmi-inner-scanner


======================================================================================
**Installation:**

Step 1. install with npm:

```bash
npm install januslo/react-natvie-sunmi-inner-printer --save
```

or you may need to install via the clone address directly:

```bash 
npm install https://github.com/januslo/react-native-sunmi-inner-printer.git --save
```

Step 2:

Links this plugin to your project.

```bash
react-native link react-natvie-sunmi-inner-printer
```

or you may need to link manually 
* modify settings.gradle

```javascript 
include ':react-native-sunmi-inner-printer'
project(':react-native-sunmi-inner-printer').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-sunmi-inner-printer/android')
```

* modify  app/build.gradle,add dependenceie：

```javascript
compile project(':react-native-sunmi-inner-printer')
```

* adds package references to  MainPackage.java 

```java

import com.sunmi.innerprinter.SunmiInnerPrinterPackage;
...

 @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
            new SunmiInnerPrinterPackage()
      );
    }

```

Step 3: refer in the javascript:
```javascript
import SunmiInnerPrinter from 'react-native-sunmi-inner-printer';

```

**Usage & Demo:**
// TODO