package JSCrusher;
import java.util.HashSet;

public class BaseNameGenerator implements NameGenerator {

	public BaseNameGenerator() {
		namePool = new HashSet<String>();
	}
	@Override
	public String genName(String origin) {

		String newName = _genName( origin);
		if (namePool.contains(newName)) {
			return genName(origin);
		} else {
			namePool.add(newName);
			return newName;
		}

	}
	public void clear(){
		namePool.clear();
	}

	protected String _genName(String origin) {

		Double pre=Math.floor(Math.random()*0xfffffff+0x80000000);
		return origin+Integer.toHexString(pre.intValue()) ;

	}
	protected HashSet<String> namePool;

}
