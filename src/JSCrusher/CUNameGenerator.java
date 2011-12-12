package JSCrusher;

public class CUNameGenerator extends BaseNameGenerator {
	
	
	
	protected String _genName(String origin){
		String newSrc="";
		int count=  (int) Math.round(2+Math.random());
		for(int i=0;i<count;i++){
			Double flag=Math.random();
//			9600-9631 9632-9727 40960-42127	
//			4E00-9FBF
			char c=(char) ((Double)(0x4E00+(0x9FBF-0x4E00)*Math.random())).intValue();
			if(flag<0){
				newSrc+="\\u"+Integer.toHexString(c);								
			}else{
				char[] cc= new char[1];
				cc[0]=c;
				newSrc+=new String(cc);
			}				
		}
		return newSrc;
	}
}
