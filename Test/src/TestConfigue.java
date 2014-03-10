import  java.io.InputStream;  
import  java.util.Properties;  
  
/**  
 * 系统配置文件类  
 * @author sunbin  
 *  
 */   
public   class  TestConfigue  extends  Thread {  
      
    //静态属性类   
    private   static  Properties p;  
      
    /**  
     * 默认构造方法  
     */   
    public  TestConfigue(){  
        //   
    }  
      
    /**  
     * 继承Thread必须要实现的方法  
     */   
    public   void  run(){  
        while ( true ){  
            //获取classpath中配置文件   
            InputStream in = TestConfigue.class .getClassLoader().getResourceAsStream( "config.properties" );  
            if  (p ==  null ){  
                p = new  Properties();  
            }  
            try {  
                p.load(in);  
                in.close();
                Thread.sleep(10000 ); //休眠10秒后重新读取配置文件   
            }catch (Exception e){  
                e.printStackTrace();  
            }  
        }  
    }  
      
    /**  
     * 获取配置文件的实例  
     * @return  
     */   
    public  Properties getProperties(){  
        return  p;  
    }  
      
    /**  
     * 测试主程序  
     * @param args  
     */   
    public   static   void  main(String[] args){  
    	TestConfigue c = new  TestConfigue();  
        c.setDaemon(true ); //设置线程为守护线程   
        c.start();//启动线程   
        try  {  
            Thread.sleep(3000 );  
        } catch  (InterruptedException e) {  
            e.printStackTrace();  
        }  
        //重复打印配置文件的值，当修改配置文件后1秒立即生效   
        while ( true ){  
            Properties p = c.getProperties();  
            System.out.println(p.getProperty("com.test.a" ));  
        }  
          
    }  
  
}  