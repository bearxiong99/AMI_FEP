package test.string;

public class TestSplit {
	public static void main(String[] args) {
		String str="1;01:00,00000A0064FF,1!01:00,00000A0064FF,1@2;04:00,00000A0064FF,2@3;08:00,00000A0064FF,3@4;14:00,00000A0064FF,4";
		
		String[] strs=str.split("#");
		for(String s:strs){
			String[] day_ids=s.split("@");
			for(String day:day_ids){
				String[] sss = day.split(";");
				for(String tariff:sss){
					String[] ff = tariff.split("!");
					for(String f:ff){
						String[] dd = f.split(",");
						for(String d:dd){
							System.out.println(d);
						}
					}
				}
			}
		
		}
	}
}
