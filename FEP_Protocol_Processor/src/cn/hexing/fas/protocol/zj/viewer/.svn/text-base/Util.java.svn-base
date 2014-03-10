package cn.hexing.fas.protocol.zj.viewer;
/**
 * @filename	Util.java
 * TODO
 */
public class Util {
	public static String[] hex=new String[]{"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
	
	/**
	 * 字节数组转化为十六进制字串
	 * @param data
	 * @param start
	 * @param len
	 * @return
	 */
	public static String BytesToHex(byte[] data,int start,int len){
		StringBuffer sb=new StringBuffer();
		for(int i=start;i<start+len;i++){			
			sb.append(hex[(data[i] & 0xf0)>>4]);
			sb.append(hex[(data[i] & 0xf)]);
			sb.append(" ");
		}
		if(sb.length()>0){
			return sb.substring(0,sb.length()-1);
		}
		return "";
	}
	
	public static String ByteToHex(byte data){
		String bt="";
		bt=hex[(data & 0xf0)>>4]+hex[(data & 0xf)];		
		return bt;
	}
	
	/**
	 * 字节数组转化为十六进制字串（字节之间没有分割符）
	 * @param data
	 * @param start
	 * @param len
	 * @return
	 */
	public static String BytesToHexL(byte[] data,int start,int len){
		StringBuffer sb=new StringBuffer();
		for(int i=start;i<start+len;i++){
			sb.append(hex[(data[i] & 0xf0)>>4]);
			sb.append(hex[(data[i] & 0xf)]);
		}
		return sb.toString();
	}
	
	/**
     * hex to byte[]
     * @param hex
     * @return
     */
    public static void HexsToBytes(byte[] frame,int loc,String hex){    	
    	try{
    		int len=(hex.length()>>>1)+(hex.length() & 0x1);    		
	    	
	    	byte[] bt=hex.getBytes();
	    	int head=0;
	    	if((hex.length() & 0x1)>0){
	    		frame[loc]=(byte)AsciiToInt(bt[0]);
	    		head=1;
	    	}else{
	    		frame[loc]=(byte)((AsciiToInt(bt[0])<<4)+AsciiToInt(bt[1]));
	    		head=2;
	    	}	    	
	    	for(int i=1;i<len;i++){	    		
	    		frame[loc+i]=(byte)((AsciiToInt(bt[head])<<4)+AsciiToInt(bt[head+1]));
	    		head+=2;
	    	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}    	
    }
    
    public static int AsciiToInt(byte val){
    	int rt=val & 0xff;
    	if(val<58){
    		rt-=48;
    	}else if(rt<71){
    		rt-=55;
    	}else{
    		rt-=87;
    	}
    	return rt;
    }
    
    public static boolean validHex(String data){
    	boolean rt=true;    	
    	for(int i=0;i<data.length();i++){
    		String c=data.substring(i,i+1);
    		if(c.compareTo("0")>=0 && c.compareToIgnoreCase("9")<=0){
    			continue;
    		}
    		if(c.compareToIgnoreCase("A")>=0 && c.compareToIgnoreCase("F")<=0){
    			continue;
    		}
    		rt=false;
    		break;
    	}
    	return rt;
    }
    
    public static byte calculateCS(byte[] data,int start,int len){
		int cs=0;
		for(int i=start;i<start+len;i++){
			cs+=(data[i] & 0xff);
			cs&=0xff;
		}
		return (byte)(cs & 0xff);
	}
    
    public static int maxArrow(String arrow){
    	int len=0;
    	int flag=0;	//0: first part 1:second part
    	int style=-1;	//0: style 0 1:style 1	-1:not sure which style
    	int lastc=0;	//0: arrow01 1:arrow02 2:handle01 3:handle02
    	String arrow01=">";
    	String arrow02="<";
    	String handle01="-";
    	String handle02="=";
    	
    	//style 0: ---- or ===== +>
    	//style 1:< +  ---- or =====
    	if(arrow!=null){
    		String strm=arrow.trim();
    		if(strm.length()>0){    			
    			flag=0;
    			style=-1;
    			int alen=0;
    			
    			for(int i=0;i<strm.length();i++){
    				String strc=strm.substring(i, i+1);
    				
    				if(strc.equals(arrow01)){    					
    					if(style<0){
    						//oh my god,why this
    						if(len<=0){
    							len=1;
    						}    						
    					}else if(style==0){
    						//ok,a arrow burn
    						alen+=1;
    						if(alen>len){
    							len=alen;
    						}
    						alen=0;
    						flag=0;
        					style=-1;
    					}else{
    						//oh,no
    						if(alen>len){
    							len=alen;
    						}
    						alen=0;
    						flag=0;
        					style=-1;
    					}
    					lastc=0;
    				}else if(strc.equals(arrow02)){
    					if(style<0){
    						//hi,a arrow head
    						style=1;
    						alen=1;
    						flag=0;
    					}else if(style==0){
    						//oh,shit,another arrow head
    						style=1;
    						alen=1;
    						flag=0;
    					}else{
    						if(len<=0 || alen>len){
    							len=alen;
    						}
    						style=1;
    						alen=1;
    						flag=0;
    					}
    					lastc=1;
    				}else if(strc.equals(handle01)){
    					if(style<0){
    						//oh,a arrow tail
    						style=0;
    						alen=1;
    						flag=0;
    					}else if(style==0){
    						if(lastc==2){
    							//a growth tail
    							alen+=1;
    						}else{
    							alen=1;
    						}
    					}else{
    						if(lastc==2 || lastc==1){
    							//a growth tail
    							alen+=1;
    						}else{
    							//a style 1 arrow burn
    							if(alen>len){
    								len=alen;
    							}
    							alen=1;
    	    					flag=0;
    	    					style=0;	//a tail here
    						}
    					}
    					lastc=2;
    				}else if(strc.equals(handle02)){
    					if(style<0){
    						//oh,a arrow tail
    						style=0;
    						alen=1;
    						flag=0;
    					}else if(style==0){
    						if(lastc==3){
    							//a growth tail
    							alen+=1;
    						}else{
    							alen=1;
    						}
    					}else{
    						if(lastc==3 || lastc==1){
    							//a growth tail
    							alen+=1;
    						}else{
    							//a style 1 arrow burn
    							if(alen>len){
    								len=alen;
    							}
    							alen=1;
    	    					flag=0;
    	    					style=0;	//a tail here
    						}
    					}    					
    					lastc=3;
    				}else{
    					//不支持的字符，可能有完整箭头发现
    					if(alen>0 && alen>len){
    						len=alen;    						
    					}
    					alen=0;
    					flag=0;
    					style=-1;
    				}
    			}
    			if(style==1 && alen>len){
    				len=alen;
    			}
    		}
    	}
    	return len;
    }
}
