package com.iplay.car.common.protocol;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class ProtocolAgreementByte {
	private byte[] msg;
	private String orderName;
	private byte[] firstSymbol_byts = { 35, 35, 35, 35 };
	private byte[] endSymbol_byts = { 35, 35, 35, 35 };
	private byte[] orderName_byts = new byte[4];
	private byte[] version_bytes = new byte[4];
	private byte[] encryptionType_byts = new byte[4];
	private byte[] mesSize_byts = new byte[4];
	private byte[] clientCode_byts = new byte[8];
	private byte[] alleyway_byts = new byte[4];
	private byte[] password_byts = new byte[4];
	private byte[] flowId_byts = new byte[2];
	private byte fcs_byts = 0;
	private byte content_byts[];
	private String contentStr;
	private String versionStr;
	private String clientCodeStr;

	private int isReply = 0;
	private List<ProtocolAgreementByte> proaList;

	public ProtocolAgreementByte() {
	}

	public byte[] getFlowId_byts() {
		return flowId_byts;
	}

	public void setFlowId_byts(byte[] flowId_byts) {
		this.flowId_byts = flowId_byts;
	}


	public String getClientCodeStr() {
		try {
			byte b[] = {0,0,0,0,0,0,0,0};
			if(!equalsByte(b,clientCode_byts,8))
			clientCodeStr = new String(clientCode_byts, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return clientCodeStr;
	}

	public void setClientCodeStr(String clientCodeStr) {
		try {
			clientCode_byts = clientCodeStr.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.clientCodeStr = clientCodeStr;
	}
	
	public String getOrderName() {
		try {
			byte b[] = {0, 0, 0, 0};
			if(!equalsByte(b,orderName_byts,4))
			orderName = new String(orderName_byts, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return orderName;
	}

	public void setOrderName(String orderName) {
		try {
			orderName_byts = orderName.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.orderName = orderName;
	}

	public byte getFcs_byts() {
		return fcs_byts;
	}

	public void setFcs_byts(byte fcs_byts) {
		this.fcs_byts = fcs_byts;
	}

	public byte[] getVersion_bytes() {
		return version_bytes;
	}

	public void setVersion_bytes(byte[] version_bytes) {
		this.version_bytes = version_bytes;
	}

	public String getVersionStr() {
		try {
			byte b[] = {0, 0, 0, 0};
			if(!equalsByte(b,version_bytes,4))
			versionStr = new String(version_bytes, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return versionStr;
	}

	public void setVersionStr(String versionStr) {
		try {
			if (null != versionStr) {
				version_bytes = versionStr.getBytes("utf-8");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.versionStr = versionStr;
	}

	public String getContentStr() {
		try {
			if (null != content_byts) {
				contentStr = new String(content_byts, "utf-8");
			} else {
				contentStr = "";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return contentStr;
	}

	public void setContentStr(String contentStr) {
		try {
			if (null != contentStr) {
				content_byts = contentStr.getBytes("utf-8");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.contentStr = contentStr;
	}

	public byte[] getContent_byts() {
		return content_byts;
	}

	public void setContent_byts(byte[] content_byts) {
		this.content_byts = content_byts;
	}

	public byte[] getMsg() {
		return msg;
	}

	public void setMsg(byte[] msg) {
		this.msg = msg;
	}

	public int getIsReply() {
		return isReply;
	}

	public void setIsReply(int isReply) {
		this.isReply = isReply;
	}

	private List<ProtocolAgreementByte> getProaList() {
		return proaList;
	}

	private void setProaList(List<ProtocolAgreementByte> proaList) {
		this.proaList = proaList;
	}

	/*
	 * 解析收到的数据 res = 0 异常 res = 2 开始符不正确 res = 3 消息长度不够 res = 4 結束符不正確,res=5 无法解析该协议版本的协议
	 */
	public int analyseData(byte[] data) {
		this.msg = data;
		int res = 0;
		try {
			if (null != msg) {
				if (msg.length > 0) {
					int size = this.msg.length;
					byte star[] = new byte[4];
					System.arraycopy(msg, 0, star, 0, 4);
					if (equalsByte(star, this.firstSymbol_byts, 4)) {
						if (size >= 43) {
							System.arraycopy(msg, 4, version_bytes, 0, 4);
							if (this.getVersionStr().equals("V150")) {
								System.arraycopy(msg, 18, mesSize_byts, 0, 4);
								int len = bytesToInt(mesSize_byts, 0);
								byte[] end = new byte[4];
								if (size > len) {
									System.arraycopy(msg, 39 + len, end, 0, 4);
									if (equalsByte(end, this.endSymbol_byts, 4)) {
										content_byts = new byte[len];
										System.arraycopy(msg, 8, orderName_byts, 0, 4);
										System.arraycopy(msg, 12, flowId_byts, 0, 2);
										System.arraycopy(msg, 14, encryptionType_byts, 0, 4);
										System.arraycopy(msg, 22, clientCode_byts, 0, 8);
										System.arraycopy(msg, 30, alleyway_byts, 0, 4);
										System.arraycopy(msg, 34, password_byts, 0, 4);
										System.arraycopy(msg, 38, content_byts, 0, len);
										setFcs_byts(msg[38 + len]);
										res = 1;
									} else {
										res = 4;
										endSymbol_byts = end;
									//	System.out.println("结束符不正确");
									}
								} else {
								//	System.out.println("协议长度不足");
									res = 3;
								}
							}else{
							//	System.out.println("无法解析该协议版本的协议");
								res = 5;
							}
						} else {
						//	System.out.println("协议长度不足");
							res = 3;
						}
					} else {
						res = 2;
						firstSymbol_byts = star;
					//	System.out.println("开始符不正确");
					}
				}
			} else {
				System.out.println("=解析协议为空=");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/*
	 * 拼装协议数据 res = 0 异常 res = 2 开始或者结束符不正确 res = 3 消息长度不够 res = 4 数据丢失或者协议不正确
	 * res = 5命令不能为空 res = 6命令长度不为4 res = 7加密类型不正确
	 */
	public int assemblyData() {
		int res = 0;
		try {
			if (null != this.firstSymbol_byts) {
				if (this.firstSymbol_byts.length == 4) {
					if (null != this.endSymbol_byts) {
						if (this.endSymbol_byts.length == 4) {
							if (null != this.orderName_byts) {
								if (this.orderName_byts.length == 4) {
									if (checkNull(this.getVersionStr())) {
										this.setVersionStr("V150");
									}
								//	System.out.println("this.getVersionStr()="+this.getVersionStr());
									int size = 0;
									int len = 0;
									if (null == this.content_byts) {
										size = 43;
										len = 0;
									} else {
										size = this.content_byts.length + 43;
										len = this.content_byts.length;
									}
									msg = new byte[size];
									this.mesSize_byts = intToBytes(len);
									System.arraycopy(this.firstSymbol_byts, 0, msg, 0, 4);
									System.arraycopy(this.version_bytes, 0, msg, 4, 4);
									System.arraycopy(this.orderName_byts, 0, msg, 8, 4);
									System.arraycopy(this.flowId_byts, 0, msg, 12, 2);
									System.arraycopy(this.encryptionType_byts, 0, msg, 14, 4);
									System.arraycopy(this.mesSize_byts, 0, msg, 18, 4);
									System.arraycopy(this.clientCode_byts, 0, msg, 22, 8);
									System.arraycopy(this.alleyway_byts, 0, msg, 30, 4);
									System.arraycopy(this.password_byts, 0, msg, 34, 4);
									if (len > 0) {
										System.arraycopy(this.content_byts, 0, msg, 38, len);
									}
									byte[] FSC = { 0 };
									System.arraycopy(FSC, 0, msg, 38 + len, 1);
									System.arraycopy(this.endSymbol_byts, 0, msg, size - 4, 4);
									res = 1;
								} else {
									res = 6;
								//	System.out.println("命令长度不为4");
								}
							} else {
								res = 5;
							//	System.out.println("命令为空");
							}
						} else {
							res = 2;
						//	System.out.println("结束符不正确");
						}
					} else {
						res = 2;
					//	System.out.println("结束符不正确");
					}
				} else {
					res = 2;
				//	System.out.println("开始符不正确");
				}
			} else {
				res = 2;
			//	System.out.println("开始符不正确");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public boolean equalsByte(byte[] data1, byte[] data2, int len) {
		if (data1 == null && data2 == null) {
			return true;
		}
		if (data1 == null || data2 == null) {
			return false;
		}
		if (data1 == data2) {
			return true;
		}
		boolean bEquals = true;
		int i;
		for (i = 0; i < data1.length && i < data2.length && i < len; i++) {
			if (data1[i] != data2[i]) {
				bEquals = false;
				break;
			}
		}
		return bEquals;
	}

	public int bytesToInt(byte[] src, int offset) {
		int value;
		value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8) | ((src[offset + 2] & 0xFF) << 16)
				| ((src[offset + 3] & 0xFF) << 24));
		return value;
	}

	public byte[] intToBytes(int value) {
		byte[] src = new byte[4];
		src[3] = (byte) ((value >> 24) & 0xFF);
		src[2] = (byte) ((value >> 16) & 0xFF);
		src[1] = (byte) ((value >> 8) & 0xFF);
		src[0] = (byte) (value & 0xFF);
		return src;
	}

	public boolean checkNull(String sInput) {
		boolean flag = false;
		try {
			if ("".equals(sInput) || null == sInput || "null".equals(sInput)) {
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	public static void main(String[] args) {
		ProtocolAgreementByte pab = new ProtocolAgreementByte();
		pab.setFlowId_byts("ab".getBytes());
		pab.setOrderName("2001");
		pab.setContentStr("哈哈");
		int t = pab.assemblyData();
		System.out.println("ttttt="+t);
		if(t == 1){
			int size = pab.getMsg().length;
			for(int i=0;i<size;i++){
				System.out.print(pab.getMsg()[i]+" ");
			}
			System.out.println();
			ProtocolAgreementByte ccc = new ProtocolAgreementByte();
			int s = ccc.analyseData(pab.getMsg());
			System.out.println("ssss="+s);
			if(s == 1){
				System.out.println("pab.getOrderName()="+pab.getOrderName());
				System.out.println("pab.getContentStr()="+pab.getContentStr());
				System.out.println("new String(pab.getFlowId_byts())="+new String(pab.getFlowId_byts()));
			}
		}
	}
}