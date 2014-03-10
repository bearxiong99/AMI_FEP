package cn.hexing.fk.bp.ws;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;




public  class XmlParser {
	public static void main(String[] args){

	}
	public static String xmlToString(String type,String rand,String div,String esam, String numData,String kid){
		StringBuffer sb = new StringBuffer(1024*30);
		try{
			if (type.equals("IdentifyAuthentication") ){//身份认证
				sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
				sb.append("<DBSET>\r\n");	
				sb.append("<R>\r\n");	
				sb.append("<C N=\"Counter\">");
				sb.append(1);
				sb.append("</C>\r\n");
				sb.append("<C N=\"Div\">");
				sb.append(div);
				sb.append("</C>\r\n");
				sb.append("</R>\r\n");
				sb.append("</DBSET>");
			}
			else if (type.equals("UserControl") ){//远程控制
				sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
				sb.append("<DBSET>\r\n");		
				sb.append("<R>\r\n");	
				sb.append("<C N=\"Counter\">");
				sb.append(1);
				sb.append("</C>\r\n");
				sb.append("<C N=\"Rand\">");
				sb.append(rand);
				sb.append("</C>\r\n");
				sb.append("<C N=\"Div\">");
				sb.append(div);
				sb.append("</C>\r\n");
				sb.append("<C N=\"Esam\">");
				sb.append(esam);
				sb.append("</C>\r\n");
				sb.append("<C N=\"NumData\">");
				sb.append(numData);
				sb.append("</C>\r\n");
				sb.append("</R>\r\n");
				sb.append("</DBSET>");
			}
			else if (type.equals("KeyUpdate") ){//密钥更新
				sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
				sb.append("<DBSET>\r\n");		
				sb.append("<R>\r\n");	
				sb.append("<C N=\"Counter\">");
				sb.append(0);
				sb.append("</C>\r\n");
				sb.append("<C N=\"Kid\">");
				sb.append(kid);
				sb.append("</C>\r\n");
				sb.append("<C N=\"Div\">");
				sb.append(div);
				sb.append("</C>\r\n");
				sb.append("<C N=\"Esam\">");
				sb.append(esam);
				sb.append("</C>\r\n");
				sb.append("<C N=\"Rand\">");
				sb.append(rand);
				sb.append("</C>\r\n");								
				sb.append("<C N=\"NumData\">");
				sb.append(numData);
				sb.append("</C>\r\n");
				sb.append("</R>\r\n");
				sb.append("</DBSET>");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return sb.toString();	
	}
	public static Object stringToXml(String input,String type){
		String flag="",rand="",endata="",dataOut="",data="",dataKey="",dataMac="";		
		StringReader sr = new StringReader(input);
		InputSource is = new InputSource(sr);
		SAXBuilder sb = new org.jdom.input.SAXBuilder();
		try{
			Document doc = sb.build(is);
			List<Element> elementRs=doc.getRootElement().getChildren();
			ArrayList<String> texts=new ArrayList<String>();
			for(Iterator<Element> iterR=elementRs.iterator();iterR.hasNext();){
				Element itemR=(Element)iterR.next();
				if (itemR.getName().equals("R")){
					List<Element> elementCs=itemR.getChildren();
					for(Iterator<Element> iterC=elementCs.iterator();iterC.hasNext();){
						Element itemC=(Element)iterC.next();
						String value=itemC.getAttributeValue("N");
						if (value==null)
							continue;
						if (value.equalsIgnoreCase("FLAG")){
							flag=itemC.getValue();
						}
						else if(value.equalsIgnoreCase("RAND")){
							rand=itemC.getValue();
						}
						else if(value.equalsIgnoreCase("ENDATA")){
							endata=itemC.getValue();
						}
						else if(value.equalsIgnoreCase("DATAOUT")){
							dataOut=itemC.getValue();
						}
						else if(value.equalsIgnoreCase("DATA")){
							data=itemC.getValue();
						}
						else if(value.equalsIgnoreCase("DATAKEY")){
							dataKey=itemC.getValue();
						}
						else if(value.equalsIgnoreCase("DATAMAC")){
							dataMac=itemC.getValue();
						}
					}
				}								
			}			
		}catch(Exception ex){
			return null;
		}	
		if (type.equals("IdentifyAuthentication") ){//身份认证
			IdentifyAuthentication identify=new IdentifyAuthentication();
			identify.setFlag(flag);
			identify.setRand(rand);
			identify.setEndata(endata);
			return identify;
		}
		else if (type.equals("UserControl") ){//远程控制
			UserControl userControl=new UserControl();
			userControl.setFlag(flag);
			userControl.setDataOut(dataOut);
			return userControl;
		}
		else if (type.equals("KeyUpdate") ){//密钥更新
			KeyUpdate keyUpdate=new KeyUpdate();
			keyUpdate.setFlag(flag);
			keyUpdate.setData(data);
			keyUpdate.setDataKey(dataKey);
			keyUpdate.setDataMac(dataMac);
			return keyUpdate;
		}
		else
			return null;
	}

}
