package config;

import java.util.Hashtable;
import java.util.logging.Level;

/**
 * User: zoran
 * Date: 2006-maj-25
 * Time: 01:35:10
 * zoran@medorian.com
 */
public class DebugLevels {
	private static Hashtable<String, Level> levels = new Hashtable<String,Level>();
	static {
		levels.put("objects.DictWord",Level.FINE);
		levels.put("objects.GenericPageCathegoryItem",Level.FINE);
		levels.put("views.FAQCathegoryItemView",Level.FINE);
		levels.put("pageservlets.ajax.AjaxDetectionHandler",Level.FINE);
		levels.put("pageservlets.AjaxHandler",Level.FINE);
		levels.put("products.ProductPageHandler",Level.FINE);
		levels.put("basket.BasketHandler",Level.INFO);
        levels.put("filters.MainFilter",Level.INFO);
    }

	public static Level getLevel(String className){
		 if ( levels.containsKey(className)){
			 return levels.get(className);
		 }
		return Level.INFO;
	}
}