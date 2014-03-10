import  java.io.InputStream;  
import  java.util.Properties;  
  
/**  
 * ϵͳ�����ļ���  
 * @author sunbin  
 *  
 */   
public   class  TestConfigue  extends  Thread {  
      
    //��̬������   
    private   static  Properties p;  
      
    /**  
     * Ĭ�Ϲ��췽��  
     */   
    public  TestConfigue(){  
        //   
    }  
      
    /**  
     * �̳�Thread����Ҫʵ�ֵķ���  
     */   
    public   void  run(){  
        while ( true ){  
            //��ȡclasspath�������ļ�   
            InputStream in = TestConfigue.class .getClassLoader().getResourceAsStream( "config.properties" );  
            if  (p ==  null ){  
                p = new  Properties();  
            }  
            try {  
                p.load(in);  
                in.close();
                Thread.sleep(10000 ); //����10������¶�ȡ�����ļ�   
            }catch (Exception e){  
                e.printStackTrace();  
            }  
        }  
    }  
      
    /**  
     * ��ȡ�����ļ���ʵ��  
     * @return  
     */   
    public  Properties getProperties(){  
        return  p;  
    }  
      
    /**  
     * ����������  
     * @param args  
     */   
    public   static   void  main(String[] args){  
    	TestConfigue c = new  TestConfigue();  
        c.setDaemon(true ); //�����߳�Ϊ�ػ��߳�   
        c.start();//�����߳�   
        try  {  
            Thread.sleep(3000 );  
        } catch  (InterruptedException e) {  
            e.printStackTrace();  
        }  
        //�ظ���ӡ�����ļ���ֵ�����޸������ļ���1��������Ч   
        while ( true ){  
            Properties p = c.getProperties();  
            System.out.println(p.getProperty("com.test.a" ));  
        }  
          
    }  
  
}  