package com.sunmi.innerprinter;

import android.content.BroadcastReceiver;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Promise;
import android.widget.Toast;

import java.util.Map;
import java.io.IOException;

import android.os.RemoteException;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;
import android.graphics.Bitmap;

import java.nio.charset.StandardCharsets;

import android.util.Log;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import android.content.IntentFilter;
import com.sunmi.peripheral.printer.*;

import java.util.Map;
import java.util.HashMap;

public class SunmiInnerPrinterModule extends ReactContextBaseJavaModule {
    public static ReactApplicationContext reactApplicationContext = null;
    private BitmapUtils bitMapUtils;
    private PrinterReceiver receiver = new PrinterReceiver();
    private SunmiPrinterService printerService = null;
    private boolean transactionMode = false;

    // 缺纸异常
    public final static String OUT_OF_PAPER_ACTION = "woyou.aidlservice.jiuv5.OUT_OF_PAPER_ACTION";
    // 打印错误
    public final static String ERROR_ACTION = "woyou.aidlservice.jiuv5.ERROR_ACTION";
    // 可以打印
    public final static String NORMAL_ACTION = "woyou.aidlservice.jiuv5.NORMAL_ACTION";
    // 开盖子
    public final static String COVER_OPEN_ACTION = "woyou.aidlservice.jiuv5.COVER_OPEN_ACTION";
    // 关盖子异常
    public final static String COVER_ERROR_ACTION = "woyou.aidlservice.jiuv5.COVER_ERROR_ACTION";
    // 切刀异常1－卡切刀
    public final static String KNIFE_ERROR_1_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_1";
    // 切刀异常2－切刀修复
    public final static String KNIFE_ERROR_2_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_2";
    // 打印头过热异常
    public final static String OVER_HEATING_ACITON = "woyou.aidlservice.jiuv5.OVER_HEATING_ACITON";
    // 打印机固件开始升级
    public final static String FIRMWARE_UPDATING_ACITON = "woyou.aidlservice.jiuv5.FIRMWARE_UPDATING_ACITON";

    private InnerPrinterCallback printerCallback = new InnerPrinterCallback () {
        @Override
        public void onDisconnected() {
          Log.d(TAG, "Printer service connected");
          printerService = null;
        }

        @Override
        public void onConnected(SunmiPrinterService service) {
          Log.d(TAG, "Printer service connected");
          printerService = service;
        }
    };

    private static final String TAG = "SunmiInnerPrinterModule";

    public SunmiInnerPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactApplicationContext = reactContext;

        try {
            InnerPrinterManager.getInstance().bindService(reactApplicationContext, printerCallback);
        } catch (Exception e) {
            Log.e(TAG, "ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        bitMapUtils = new BitmapUtils(reactContext);
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(OUT_OF_PAPER_ACTION);
        mFilter.addAction(ERROR_ACTION);
        mFilter.addAction(NORMAL_ACTION);
        mFilter.addAction(COVER_OPEN_ACTION);
        mFilter.addAction(COVER_ERROR_ACTION);
        mFilter.addAction(KNIFE_ERROR_1_ACTION);
        mFilter.addAction(KNIFE_ERROR_2_ACTION);
        mFilter.addAction(OVER_HEATING_ACITON);
        mFilter.addAction(FIRMWARE_UPDATING_ACITON);
        getReactApplicationContext().registerReceiver(receiver, mFilter);
        Log.d("PrinterReceiver", "------------ init ");
    }

    @Override
    public String getName() {
        return "SunmiInnerPrinter";
    }


    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        final Map<String, Object> constantsChildren = new HashMap<>();

        constantsChildren.put("OUT_OF_PAPER_ACTION", OUT_OF_PAPER_ACTION);
        constantsChildren.put("ERROR_ACTION", ERROR_ACTION);
        constantsChildren.put("NORMAL_ACTION", NORMAL_ACTION);
        constantsChildren.put("COVER_OPEN_ACTION", COVER_OPEN_ACTION);
        constantsChildren.put("COVER_ERROR_ACTION", COVER_ERROR_ACTION);
        constantsChildren.put("KNIFE_ERROR_1_ACTION", KNIFE_ERROR_1_ACTION);
        constantsChildren.put("KNIFE_ERROR_2_ACTION", KNIFE_ERROR_2_ACTION);
        constantsChildren.put("OVER_HEATING_ACITON", OVER_HEATING_ACITON);
        constantsChildren.put("FIRMWARE_UPDATING_ACITON", FIRMWARE_UPDATING_ACITON);

        constants.put("Constants", constantsChildren);

        constants.put("hasPrinter", hasPrinter());

        try {
            constants.put("printerVersion", getPrinterVersion());
        } catch (Exception e) {
            // Log and ignore for it is not the madatory constants.
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
        try {
            constants.put("printerSerialNo", getPrinterSerialNo());
        } catch (Exception e) {
            // Log and ignore for it is not the madatory constants.
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
        try {
            constants.put("printerModal", getPrinterModal());
        } catch (Exception e) {
            // Log and ignore for it is not the madatory constants.
            Log.i(TAG, "ERROR: " + e.getMessage());
        }

        return constants;
    }


    /**
     * 初始化打印机，重置打印机的逻辑程序，但不清空缓存区数据，因此
     * 未完成的打印作业将在重置后继续
     *
     * @return
     */
    @ReactMethod
    public void printerInit(final Promise p) {
        final SunmiPrinterService service = printerService;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    service.printerInit(new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode) {
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 打印机自检，打印机会打印自检页
     *
     * @param callback 回调
     */
    @ReactMethod
    public void printerSelfChecking(final Promise p) {
        final SunmiPrinterService service = printerService;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    service.printerSelfChecking(new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode) {
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 获取打印机板序列号
     */
    @ReactMethod
    public void getPrinterSerialNo(final Promise p) {
        try {
            p.resolve(getPrinterSerialNo());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    private String getPrinterSerialNo() throws Exception {
        return printerService.getPrinterSerialNo();
    }

    /**
     * 获取打印机固件版本号
     */
    @ReactMethod
    public void getPrinterVersion(final Promise p) {
        try {
            p.resolve(getPrinterVersion());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    private String getPrinterVersion() throws Exception {
        return printerService.getPrinterVersion();
    }

    /**
     * 获取打印机型号
     */
    @ReactMethod
    public void getPrinterModal(final Promise p) {
        try {
            p.resolve(getPrinterModal());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    private String getPrinterModal() throws Exception {
        //Caution: This method is not fully test -- Januslo 2018-08-11
        return printerService.getPrinterModal();
    }

    @ReactMethod
    public void hasPrinter(final Promise p) {
        try {
            p.resolve(hasPrinter());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    /**
     * 是否存在打印机服务
     * return {boolean}
     */
    private boolean hasPrinter() {
        return printerService != null;
    }

    /**
     * 获取打印头打印长度
     */
    @ReactMethod
    public void getPrintedLength(final Promise p) {
        final SunmiPrinterService service = printerService;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    service.getPrintedLength(new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode) {
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 打印机走纸(强制换行，结束之前的打印内容后走纸n行)
     *
     * @param n:       走纸行数
     * @param callback 结果回调
     * @return
     */
    @ReactMethod
    public void lineWrap(int n, final Promise p) {
        final SunmiPrinterService service = printerService;
        final int count = n;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            boolean resolved = false;

            @Override
            public void run() {
                try {
                    service.lineWrap(count, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;

                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode && !resolved) {
                        resolved = true;
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 使用原始指令打印
     *
     * @param data     指令
     * @param callback 结果回调
     */
    @ReactMethod
    public void sendRAWData(String base64EncriptedData, final Promise p) {
        final SunmiPrinterService service = printerService;
        final byte[] d = Base64.decode(base64EncriptedData, Base64.DEFAULT);
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    service.sendRAWData(d, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode) {
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 设置对齐模式，对之后打印有影响，除非初始化
     *
     * @param alignment: 对齐方式 0--居左 , 1--居中, 2--居右
     * @param callback   结果回调
     */
    @ReactMethod
    public void setAlignment(int alignment, final Promise p) {
        final SunmiPrinterService service = printerService;
        final int align = alignment;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            boolean resolved = false;

            @Override
            public void run() {
                try {
                    service.setAlignment(align, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;

                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode && !resolved) {
                        resolved = true;
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 设置打印字体, 对之后打印有影响，除非初始化
     * (目前只支持一种字体"gh"，gh是一种等宽中文字体，之后会提供更多字体选择)
     *
     * @param typeface: 字体名称
     */
    @ReactMethod
    public void setFontName(String typeface, final Promise p) {
        final SunmiPrinterService service = printerService;
        final String tf = typeface;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    service.setFontName(tf, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode) {
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 设置字体大小, 对之后打印有影响，除非初始化
     * 注意：字体大小是超出标准国际指令的打印方式，
     * 调整字体大小会影响字符宽度，每行字符数量也会随之改变，
     * 因此按等宽字体形成的排版可能会错乱
     *
     * @param fontsize: 字体大小
     */
    @ReactMethod
    public void setFontSize(float fontsize, final Promise p) {
        final SunmiPrinterService service = printerService;
        final float fs = fontsize;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            boolean resolved = false;

            @Override
            public void run() {
                try {
                    service.setFontSize(fs, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;

                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode && !resolved) {
                        resolved = true;
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }


    /**
     * 打印指定字体的文本，字体设置只对本次有效
     *
     * @param text:     要打印文字
     * @param typeface: 字体名称（目前只支持"gh"字体）
     * @param fontsize: 字体大小
     */
    @ReactMethod
    public void printTextWithFont(String text, String typeface, float fontsize, final Promise p) {
        final SunmiPrinterService service = printerService;
        final String txt = text;
        final String tf = typeface;
        final float fs = fontsize;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    service.printTextWithFont(txt, tf, fs, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode) {
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 打印表格的一行，可以指定列宽、对齐方式
     *
     * @param colsTextArr  各列文本字符串数组
     * @param colsWidthArr 各列宽度数组(以英文字符计算, 每个中文字符占两个英文字符, 每个宽度大于0)
     * @param colsAlign    各列对齐方式(0居左, 1居中, 2居右)
     *                     备注: 三个参数的数组长度应该一致, 如果colsText[i]的宽度大于colsWidth[i], 则文本换行
     */
    @ReactMethod
    public void printColumnsText(ReadableArray colsTextArr, ReadableArray colsWidthArr, ReadableArray colsAlign, final Promise p) {
        final SunmiPrinterService service = printerService;
        final String[] clst = new String[colsTextArr.size()];
        for (int i = 0; i < colsTextArr.size(); i++) {
            clst[i] = colsTextArr.getString(i);
        }
        final int[] clsw = new int[colsWidthArr.size()];
        for (int i = 0; i < colsWidthArr.size(); i++) {
            clsw[i] = colsWidthArr.getInt(i);
        }
        final int[] clsa = new int[colsAlign.size()];
        for (int i = 0; i < colsAlign.size(); i++) {
            clsa[i] = colsAlign.getInt(i);
        }
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    service.printColumnsText(clst, clsw, clsa, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode) {
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }


    /**
     * 打印图片
     *
     * @param bitmap: 图片bitmap对象(最大宽度384像素，超过无法打印并且回调callback异常函数)
     */
    @ReactMethod
    public void printBitmap(String data, int width, int height, final Promise p) {
        try {
            final SunmiPrinterService service = printerService;
            byte[] decoded = Base64.decode(data, Base64.DEFAULT);
            final Bitmap bitMap = bitMapUtils.decodeBitmap(decoded, width, height);
            ThreadPoolManager.getInstance().executeTask(new Runnable() {
                boolean resolved = false;

                @Override
                public void run() {
                    try {
                        service.printBitmap(bitMap, new ICallback.Stub() {
                            @Override
                            public void onPrintResult(int par1, String par2) {
                                Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                            }

                            @Override
                            public void onRunResult(boolean isSuccess) {
                                if (resolved) {
                                    return;
                                }

                                resolved = true;

                                if (isSuccess) {
                                    p.resolve(null);
                                } else {
                                    p.reject("0", isSuccess + "");
                                }
                            }

                            @Override
                            public void onReturnString(String result) {
                                if (resolved) {
                                    return;
                                }

                                resolved = true;
                                p.resolve(result);
                            }

                            @Override
                            public void onRaiseException(int code, String msg) {
                                if (resolved) {
                                    return;
                                }

                                resolved = true;
                                p.reject("" + code, msg);
                            }
                        });

                        if (transactionMode && !resolved) {
                            resolved = true;
                            p.resolve(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i(TAG, "ERROR: " + e.getMessage());
                        p.reject("" + 0, e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
    }

    /**
     * 打印一维条码
     *
     * @param data:         条码数据
     * @param symbology:    条码类型
     *                      0 -- UPC-A，
     *                      1 -- UPC-E，
     *                      2 -- JAN13(EAN13)，
     *                      3 -- JAN8(EAN8)，
     *                      4 -- CODE39，
     *                      5 -- ITF，
     *                      6 -- CODABAR，
     *                      7 -- CODE93，
     *                      8 -- CODE128
     * @param height:       条码高度, 取值1到255, 默认162
     * @param width:        条码宽度, 取值2至6, 默认2
     * @param textposition: 文字位置 0--不打印文字, 1--文字在条码上方, 2--文字在条码下方, 3--条码上下方均打印
     */
    @ReactMethod
    public void printBarCode(String data, int symbology, int height, int width, int textposition, final Promise p) {
        final SunmiPrinterService service = printerService;
        Log.d(TAG, "printBarCode - " + data);
        final String d = data;
        final int s = symbology;
        final int h = height;
        final int w = width;
        final int tp = textposition;

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            boolean resolved = false;

            @Override
            public void run() {
                try {
                    service.printBarCode(d, s, h, w, tp, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;

                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode && !resolved) {
                        resolved = true;
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 打印二维条码
     *
     * @param data:       二维码数据
     * @param modulesize: 二维码块大小(单位:点, 取值 1 至 16 )
     * @param errorlevel: 二维码纠错等级(0 至 3)，
     *                    0 -- 纠错级别L ( 7%)，
     *                    1 -- 纠错级别M (15%)，
     *                    2 -- 纠错级别Q (25%)，
     *                    3 -- 纠错级别H (30%)
     */
    @ReactMethod
    public void printQRCode(String data, int modulesize, int errorlevel, final Promise p) {
        final SunmiPrinterService service = printerService;
        Log.d(TAG, "printQRCode - " + data);
        final String d = data;
        final int size = modulesize;
        final int level = errorlevel;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            boolean resolved = false;

            @Override
            public void run() {
                try {
                    service.printQRCode(d, size, level, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;

                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode && !resolved) {
                        resolved = true;
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 打印文字，文字宽度满一行自动换行排版，不满一整行不打印除非强制换行
     * 文字按矢量文字宽度原样输出，即每个字符不等宽
     *
     * @param text: 要打印的文字字符串
     */
    @ReactMethod
    public void printOriginalText(String text, final Promise p) {
        final SunmiPrinterService service = printerService;
        Log.d(TAG, "printOriginalText - " + text);
        final String txt = text;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    service.printOriginalText(txt, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode) {
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 打印缓冲区内容
     */
    @ReactMethod
    public void commitPrinterBuffer() {
        final SunmiPrinterService service = printerService;
        Log.d(TAG, "commitPrinterBuffer");
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    service.commitPrinterBuffer();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }

    /**
     * 进入缓冲模式，所有打印调用将缓存，调用commitPrinterBuffe()后打印
     *
     * @param clean: 是否清除缓冲区内容
     */
    @ReactMethod
    public void enterPrinterBuffer(boolean clean) {
        final SunmiPrinterService service = printerService;
        Log.d(TAG, "enterPrinterBuffer - clean: " + clean);
        transactionMode = true;
        final boolean c = clean;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    service.enterPrinterBuffer(c);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }

    /**
     * 退出缓冲模式
     *
     * @param commit: 是否打印出缓冲区内容
     */
    @ReactMethod
    public void exitPrinterBuffer(boolean commit) {
        final SunmiPrinterService service = printerService;
        Log.d(TAG, "exitPrinterBuffer - commit: " + commit);
        transactionMode = false;
        final boolean com = commit;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    service.exitPrinterBuffer(com);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }


    @ReactMethod
    public void printString(String message, final Promise p) {
        final SunmiPrinterService service = printerService;
        Log.d(TAG, "printString - message: " + message);
        final String msgs = message;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            boolean resolved = false;

            @Override
            public void run() {
                try {
                    service.printText(msgs, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;

                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            if (resolved) {
                                return;
                            }

                            resolved = true;
                            p.reject("" + code, msg);
                        }
                    });

                    if (transactionMode && !resolved) {
                        resolved = true;
                        p.resolve(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    @ReactMethod
    public void clearBuffer() {
        final SunmiPrinterService service = printerService;
        Log.d(TAG, "clearBuffer");
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    service.clearBuffer();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }

    @ReactMethod
    public void exitPrinterBufferWithCallback(final boolean commit, final Callback callback) {
        final SunmiPrinterService service = printerService;
        Log.d(TAG, "exitPrinterBufferWithCallback - commit: " + commit);
        transactionMode = false;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    service.exitPrinterBufferWithCallback(commit, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int code, String msg) {
                            Log.i(TAG, "ON PRINT RES: " + code + ", " + msg);
                            if (code == 0)
                                callback.invoke(true);
                            else
                                callback.invoke(false);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            callback.invoke(isSuccess);
                        }

                        @Override
                        public void onReturnString(String result) {
                            // callback.invoke(isSuccess);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            callback.invoke(false);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    callback.invoke(false);
                }
            }
        });
    }
}
