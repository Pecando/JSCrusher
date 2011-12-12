package JSCrusher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.mozilla.javascript.tools.SourceReader;

/**
 * @author 朱昱昭
 */
public class Main {

	public static void main(String args[]) {
		Main main = new Main();
//		args = main.processOptions(args);
		main.processSource(args[0],args[1]);
	}

	public Main() {
	}
	
	public String[] processOptions(String args[]) {
		return null;
	}

	public void processSource(String filename, String outName) {

			File f = new File(filename);
			
			String source = readSource(f);
			if (source == null){
				System.err.println("Can not read file.");
				System.exit(1);
			}
			
			JSCrusher crusher = new JSCrusher();
			crusher.setNameGen(new CUNameGenerator());
			String rst = crusher.crush(source);
			
//			crusher.printModifierable();


			File o = new File(outName);
			try {
				byte[] bytes = rst.getBytes("utf-8");
				FileOutputStream os = new FileOutputStream(o);
				try {
					os.write(bytes);
				} finally {
					os.close();
				}
			} catch (IOException ioe) {
				System.err.println(ioe);
				System.exit(1);
			}
	}

	private String readSource(File f) {
		String absPath = f.getAbsolutePath();
		if (!f.isFile()) {
			System.err.println("No file specified.");
			return null;
		}
		try {
			return (String) SourceReader.readFileOrUrl(absPath, true, "utf-8");
		} catch (Exception ex) {
			System.err.println("IO error.");
		}
		return null;
	}

}
