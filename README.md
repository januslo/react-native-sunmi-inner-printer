# react-native-sunmi-inner-printer

http://docs.sunmi.com/htmls/index.html?lang=zh##V1文档资源
根据商米V1文档开发打印接口
(React native plugin Referring the sunmi V1 printer document and demos)

**Caution: this is not the official project. I share it because I am working on this device but no any official support in react-native It's welcome to ask any question about the usage,problems or feature required, I will support ASAP.**

for scanner, refer this: https://github.com/januslo/react-native-sunmi-inner-scanner

## Installation:

**Step 1.**

install with npm: [Check in NPM](https://www.npmjs.com/package/react-native-sunmi-inner-scanner)

```bash
npm install react-natvie-sunmi-inner-printer --save
```

or you may need to install via the clone address directly:

```bash 
npm install https://github.com/januslo/react-native-sunmi-inner-printer.git --save
```

**Step 2:**

Links this plugin to your project.

```bash
react-native link react-native-sunmi-inner-printer
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

**Step 3:**

refer in the javascript:
```javascript
import SunmiInnerPrinter from 'react-native-sunmi-inner-printer';

```

## Usage & Demo:
See examples folder of the source code that you can find a simple example of printing receipt.
// TODO

## API

### Printer Status

|  Name | Description |
|:-----:|:-----------:|
| OUT_OF_PAPER_ACTION | 缺纸异常 |
| ERROR_ACTION | 打印错误 |
| NORMAL_ACTION | 可以打印 |
| COVER_OPEN_ACTION | 开盖子 |
| COVER_ERROR_ACTION | 关盖子异常 |
| KNIFE_ERROR_1_ACTION | 切刀异常1－卡切刀 |
| KNIFE_ERROR_2_ACTION | 切刀异常2－切刀修复 |
| OVER_HEATING_ACITON | 打印头过热异常 |
| FIRMWARE_UPDATING_ACITON | 打印机固件开始升级 |

#### Example

```javascript
import React, { Component } from 'react';
import { View, Text, DeviceEventEmitter } from 'react-native';
import SunmiInnerPrinter from 'react-native-sunmi-inner-printer';

class PrinterComponent extends Component {
    componentWillMount() {
        this._printerStatusListener = DeviceEventEmitter.addListener('PrinterStatus', action => {
            switch(action) {
                case SunmiInnerPrinter.Constants.NORMAL_ACTION:   // 可以打印
                    // your code
                    break;
                case SunmiInnerPrinter.Constants.OUT_OF_PAPER_ACTION:  // 缺纸异常
                    // your code
                    break;
                case SunmiInnerPrinter.Constants.COVER_OPEN_ACTION:   // 开盖子
                    // your code
                    break;
                default:
                    // your code
            }
        });
    }
    
    componentWillUnmount() {
        this._printerStatusListener.remove();
    }

    render() {
        return (
            <View>
                <Text>Hello World!</Text>
            </View>
        )
    }
}
```
