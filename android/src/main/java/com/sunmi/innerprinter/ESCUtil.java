package com.sunmi.innerprinter;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ESCUtil {

	public static final byte ESC = 27;// 换码
	public static final byte FS = 28;// 文本分隔符
	public static final byte GS = 29;// 组分隔符
	public static final byte DLE = 16;// 数据连接换码
	public static final byte EOT = 4;// 传输结束
	public static final byte ENQ = 5;// 询问字符
	public static final byte SP = 32;// 空格
	public static final byte HT = 9;// 横向列表
	public static final byte LF = 10;// 打印并换行（水平定位）
	public static final byte CR = 13;// 归位键
	public static final byte FF = 12;// 走纸控制（打印并回到标准模式（在页模式下） ）
	public static final byte CAN = 24;// 作废（页模式下取消打印数据 ）

	// ------------------------打印机初始化-----------------------------

	private static String hexStr = "0123456789ABCDEF";
	private static String[] binaryArray = { "0000", "0001", "0010", "0011",
			"0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
			"1100", "1101", "1110", "1111" };

private static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode(new String(new byte[] { src0 })).byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode(new String(new byte[] { src1 })).byteValue();
		byte ret = (byte) (_b0 | _b1);
		byte aret = Byte.decode("0x" + ret).byteValue();

		return aret;
	}
	public static String binaryStrToHexString(String binaryStr) {
		String hex = "";
		String f4 = binaryStr.substring(0, 4);
		String b4 = binaryStr.substring(4, 8);
		for (int i = 0; i < binaryArray.length; i++) {
			if (f4.equals(binaryArray[i]))
				hex += hexStr.substring(i, i + 1);
		}
		for (int i = 0; i < binaryArray.length; i++) {
			if (b4.equals(binaryArray[i]))
				hex += hexStr.substring(i, i + 1);
		}

		return hex;
	}
	
		public static byte[] HexStringToBinary(String hexString) {		
		int len = hexString.length() / 2;
		byte[] bytes = new byte[len];
		byte high = 0;
		byte low = 0;
		for (int i = 0; i < len; i++) {			
			high = (byte) ((hexStr.indexOf(hexString.charAt(2 * i))) << 4);
			low = (byte) hexStr.indexOf(hexString.charAt(2 * i + 1));
			bytes[i] = (byte) (high & 0xF0 | low & 0x0F);
		}
		return bytes;
	}

	public static List<String> binaryListToHexStringList(List<String> list) {
		List<String> hexList = new ArrayList<String>();
		for (String binaryStr : list) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < binaryStr.length(); i += 8) {
				String str = binaryStr.substring(i, i + 8);
				String hexString = binaryStrToHexString(str);
				sb.append(hexString);
			}
			hexList.add(sb.toString());
		}
		return hexList;

	}
	public static byte[] sysCopy(List<byte[]> srcArrays) {
		int len = 0;
		for (byte[] srcArray : srcArrays) {
			len += srcArray.length;
		}
		byte[] destArray = new byte[len];
		int destLen = 0;
		for (byte[] srcArray : srcArrays) {
			System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
			destLen += srcArray.length;
		}
		return destArray;
	}

	public static byte[] hexList2Byte(List<String> list) {
		List<byte[]> commandList = new ArrayList<byte[]>();
		for (String hexStr : list) {
			commandList.add(HexStringToBinary(hexStr));
		}
		byte[] bytes = sysCopy(commandList);
		return bytes;
	}

	/**
	 * 打印机初始化
	 * 
	 * @return
	 */
	public static byte[] init_printer() {
		byte[] result = new byte[2];
		result[0] = ESC;
		result[1] = 64;
		return result;
	}

	// ------------------------换行-----------------------------

	/**
	 * 换行
	 * 
	 * @param lineNum要换几行
	 * @return
	 */
	public static byte[] nextLine(int lineNum) {
		byte[] result = new byte[lineNum];
		for (int i = 0; i < lineNum; i++) {
			result[i] = LF;
		}

		return result;
	}

	// ------------------------下划线-----------------------------

	/**
	 * 绘制下划线（1点宽）
	 * 
	 * @return
	 */
	public static byte[] underlineWithOneDotWidthOn() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 45;
		result[2] = 1;
		return result;
	}

	/**
	 * 绘制下划线（2点宽）
	 * 
	 * @return
	 */
	public static byte[] underlineWithTwoDotWidthOn() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 45;
		result[2] = 2;
		return result;
	}

	/**
	 * 取消绘制下划线
	 * 
	 * @return
	 */
	public static byte[] underlineOff() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 45;
		result[2] = 0;
		return result;
	}

	// ------------------------加粗-----------------------------

	/**
	 * 选择加粗模式
	 * 
	 * @return
	 */
	public static byte[] boldOn() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 69;
		result[2] = 0xF;
		return result;
	}

	/**
	 * 取消加粗模式
	 * 
	 * @return
	 */
	public static byte[] boldOff() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 69;
		result[2] = 0;
		return result;
	}

	// ------------------------对齐-----------------------------

	/**
	 * 左对齐
	 * 
	 * @return
	 */
	public static byte[] alignLeft() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 97;
		result[2] = 0;
		return result;
	}

	/**
	 * 居中对齐
	 * 
	 * @return
	 */
	public static byte[] alignCenter() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 97;
		result[2] = 1;
		return result;
	}

	/**
	 * 右对齐
	 * 
	 * @return
	 */
	public static byte[] alignRight() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 97;
		result[2] = 2;
		return result;
	}

	/**
	 * 水平方向向右移动col列
	 * 
	 * @param col
	 * @return
	 */
	public static byte[] set_HT_position(byte col) {
		byte[] result = new byte[4];
		result[0] = ESC;
		result[1] = 68;
		result[2] = col;
		result[3] = 0;
		return result;
	}
	// ------------------------字体变大-----------------------------

	/**
	 * 字体变大为标准的n倍
	 * 
	 * @param num
	 * @return
	 */
	public static byte[] fontSizeSetBig(int num) {
		byte realSize = 0;
		switch (num) {
		case 1:
			realSize = 0;
			break;
		case 2:
			realSize = 17;
			break;
		case 3:
			realSize = 34;
			break;
		case 4:
			realSize = 51;
			break;
		case 5:
			realSize = 68;
			break;
		case 6:
			realSize = 85;
			break;
		case 7:
			realSize = 102;
			break;
		case 8:
			realSize = 119;
			break;
		}
		byte[] result = new byte[3];
		result[0] = 29;
		result[1] = 33;
		result[2] = realSize;
		return result;
	}

	// ------------------------字体变小-----------------------------

	/**
	 * 字体取消倍宽倍高
	 * 
	 * @param num
	 * @return
	 */
	public static byte[] fontSizeSetSmall(int num) {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 33;

		return result;
	}

	// ------------------------切纸-----------------------------

	/**
	 * 进纸并全部切割
	 * 
	 * @return
	 */
	public static byte[] feedPaperCutAll() {
		byte[] result = new byte[4];
		result[0] = GS;
		result[1] = 86;
		result[2] = 65;
		result[3] = 0;
		return result;
	}

	/**
	 * 进纸并切割（左边留一点不切）
	 * 
	 * @return
	 */
	public static byte[] feedPaperCutPartial() {
		byte[] result = new byte[4];
		result[0] = GS;
		result[1] = 86;
		result[2] = 66;
		result[3] = 0;
		return result;
	}

	// ------------------------切纸-----------------------------
	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}

	public static byte[] byteMerger(byte[][] byteList) {

		int length = 0;
		for (int i = 0; i < byteList.length; i++) {
			length += byteList[i].length;
		}
		byte[] result = new byte[length];

		int index = 0;
		for (int i = 0; i < byteList.length; i++) {
			byte[] nowByte = byteList[i];
			for (int k = 0; k < byteList[i].length; k++) {
				result[index] = nowByte[k];
				index++;
			}
		}
		for (int i = 0; i < index; i++) {
			// CommonUtils.LogWuwei("", "result[" + i + "] is " + result[i]);
		}
		return result;
	}

	// --------------------
	public static byte[] generateMockData() {
		try {
			byte[] next2Line = ESCUtil.nextLine(2);
			byte[] title = "出餐单（午餐）**万通中心店".getBytes("gb2312");

			byte[] boldOn = ESCUtil.boldOn();
			byte[] fontSize2Big = ESCUtil.fontSizeSetBig(3);
			byte[] center = ESCUtil.alignCenter();
			byte[] Focus = "网 507".getBytes("gb2312");
			byte[] boldOff = ESCUtil.boldOff();
			byte[] fontSize2Small = ESCUtil.fontSizeSetSmall(3);

			byte[] left = ESCUtil.alignLeft();
			byte[] orderSerinum = "订单编号：11234".getBytes("gb2312");
			boldOn = ESCUtil.boldOn();
			byte[] fontSize1Big = ESCUtil.fontSizeSetBig(2);
			byte[] FocusOrderContent = "韭菜鸡蛋饺子-小份（单）".getBytes("gb2312");
			boldOff = ESCUtil.boldOff();
			byte[] fontSize1Small = ESCUtil.fontSizeSetSmall(2);

			next2Line = ESCUtil.nextLine(2);

			byte[] priceInfo = "应收:22元 优惠：2.5元 ".getBytes("gb2312");
			byte[] nextLine = ESCUtil.nextLine(1);

			byte[] priceShouldPay = "实收:19.5元".getBytes("gb2312");
			nextLine = ESCUtil.nextLine(1);

			byte[] takeTime = "取餐时间:2015-02-13 12:51:59".getBytes("gb2312");
			nextLine = ESCUtil.nextLine(1);
			byte[] setOrderTime = "下单时间：2015-02-13 12:35:15".getBytes("gb2312");

			byte[] tips_1 = "微信关注\"**\"自助下单每天免1元".getBytes("gb2312");
			nextLine = ESCUtil.nextLine(1);
			byte[] tips_2 = "饭后点评再奖5毛".getBytes("gb2312");
			byte[] next4Line = ESCUtil.nextLine(4);

			byte[] breakPartial = ESCUtil.feedPaperCutPartial();

			byte[][] cmdBytes = { title, nextLine, center, boldOn, fontSize2Big, Focus, boldOff, fontSize2Small,
					next2Line, left, orderSerinum, nextLine, center, boldOn, fontSize1Big, FocusOrderContent, boldOff,
					fontSize1Small, nextLine, left, next2Line, priceInfo, nextLine, priceShouldPay, next2Line, takeTime,
					nextLine, setOrderTime, next2Line, center, tips_1, nextLine, center, tips_2, next4Line,
					breakPartial };

			return ESCUtil.byteMerger(cmdBytes);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decodeBitmap(byte[] bitmapBytes) {

    Bitmap bmp = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

    int zeroCount = bmp.getWidth() % 8;
    String zeroStr = "";
    if (zeroCount > 0) {
        for (int i = 0; i < (8 - zeroCount); i++) {
            zeroStr = zeroStr + "0";
        }
    }

    List<String> list = new ArrayList<>();
    for (int i = 0; i < bmp.getHeight(); i++) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < bmp.getWidth(); j++) {
            int color = bmp.getPixel(j, i);

            int r = (color >> 16) & 0xff;
            int g = (color >> 8) & 0xff;
            int b = color & 0xff;

            // if color close to white，bit='0', else bit='1'
            if (r > 160 && g > 160 && b > 160)
                sb.append("0");
            else
                sb.append("1");
        }
        if (zeroCount > 0) {
            sb.append(zeroStr);
        }

        list.add(sb.toString());
    }

    List<String> bmpHexList = binaryListToHexStringList(list);
    List<String> commandList = new ArrayList<>();
    commandList.addAll(bmpHexList);

    return hexList2Byte(commandList);
}
public static byte[] decodeBitmap2(byte[] bitmapBytes) {
    Bitmap bmp = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
	return draw2PxPoint(bmp);
}

public static byte[] draw2PxPoint(Bitmap bmp) {
        //用来存储转换后的 bitmap 数据。为什么要再加1000，这是为了应对当图片高度无法
        //整除24时的情况。比如bitmap 分辨率为 240 * 250，占用 7500 byte，
        //但是实际上要存储11行数据，每一行需要 24 * 240 / 8 =720byte 的空间。再加上一些指令存储的开销，
        //所以多申请 1000byte 的空间是稳妥的，不然运行时会抛出数组访问越界的异常。
        int size = bmp.getWidth() * bmp.getHeight() / 8 + 1000;
        byte[] data = new byte[size];
        int k = 0;
        //设置行距为0的指令
        data[k++] = 0x1B;
        data[k++] = 0x33;
        data[k++] = 0x00;
        // 逐行打印
        for (int j = 0; j < bmp.getHeight() / 24f; j++) {
            //打印图片的指令
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33;
            data[k++] = (byte) (bmp.getWidth() % 256); //nL
            data[k++] = (byte) (bmp.getWidth() / 256); //nH
            //对于每一行，逐列打印
            for (int i = 0; i < bmp.getWidth(); i++) {
                //每一列24个像素点，分为3个字节存储
                for (int m = 0; m < 3; m++) {
                    //每个字节表示8个像素点，0表示白色，1表示黑色
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
                        data[k] += data[k] + b;
                    }
                    k++;
                }
            }
            data[k++] = 10;//换行
        }
        return data;
    }
    /**
     * 灰度图片黑白化，黑色是1，白色是0
     *
     * @param x   横坐标
     * @param y   纵坐标
     * @param bit 位图
     * @return
     */
    public static byte px2Byte(int x, int y, Bitmap bit) {
        if (x < bit.getWidth() && y < bit.getHeight()) {
            byte b;
            int pixel = bit.getPixel(x, y);
            int red = (pixel & 0x00ff0000) >> 16; // 取高两位
            int green = (pixel & 0x0000ff00) >> 8; // 取中两位
            int blue = pixel & 0x000000ff; // 取低两位
            int gray = RGB2Gray(red, green, blue);
            if (gray < 128) {
                b = 1;
            } else {
                b = 0;
            }
            return b;
        }
        return 0;
    }
     /**
     * 图片灰度的转化
     */
    private static int RGB2Gray(int r, int g, int b) {
        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b);  //灰度转化公式
        return gray;
    }
}


