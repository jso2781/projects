/**
 * 
 */
package mybatis.exception.translate;

import java.sql.SQLException;

import org.apache.ibatis.exceptions.PersistenceException;


/**
 * @author JOSEONGOK
 *
 */
public class MybatisExceptionTranslator
{
	public MybatisExceptionTranslator(RuntimeException re)
	{
		if(re instanceof PersistenceException)
		{
			if(re.getCause() instanceof PersistenceException)
			{
				re = (PersistenceException)re.getCause();
			}

			if(re.getCause() instanceof SQLException)
			{
				
			}
		}
	}

}
