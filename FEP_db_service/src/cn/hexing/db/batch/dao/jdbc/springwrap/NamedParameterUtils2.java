/**
 * 对NamedParameterUtils进行简单修改，以便把一个对象POJO作为SQL参数传入到SQL语句中。
 */
package cn.hexing.db.batch.dao.jdbc.springwrap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.util.Assert;

/**
 * 
 */
public abstract class NamedParameterUtils2 {
	/**
	 * Set of characters that qualify as parameter separators,
	 * indicating that a parameter name in a SQL String has ended.
	 */
	private static final char[] PARAMETER_SEPARATORS =
			new char[] {'"', '\'', ':', '&', ',', ';', '(', ')', '|', '=', '+', '-', '*', '%', '/', '\\', '<', '>', '^'};

	public static ParsedSql2 parseSqlStatement(String sql) {
		Assert.notNull(sql, "SQL must not be null");

		Set<String> namedParameters = new HashSet<String>();
		ParsedSql2 parsedSql = new ParsedSql2(sql);

		char[] statement = sql.toCharArray();
		boolean withinQuotes = false;
		char currentQuote = '-';
		int namedParameterCount = 0;
		int unnamedParameterCount = 0;
		int totalParameterCount = 0;

		int i = 0;
		while (i < statement.length) {
			char c = statement[i];
			if (withinQuotes) {
				if (c == currentQuote) {
					withinQuotes = false;
					currentQuote = '-';
				}
			}
			else {
				if (c == '"' || c == '\'') {
					withinQuotes = true;
					currentQuote = c;
				}
				else {
					if (c == ':' || c == '&') {
						int j = i + 1;
						if (j < statement.length && statement[j] == ':' && c == ':') {
							// Postgres-style "::" casting operator - to be skipped.
							i = i + 2;
							continue;
						}
						while (j < statement.length && !isParameterSeparator(statement[j])) {
							j++;
						}
						if (j - i > 1) {
							String parameter = sql.substring(i + 1, j);
							if (!namedParameters.contains(parameter)) {
								namedParameters.add(parameter);
								namedParameterCount++;
							}
							parsedSql.addNamedParameter(parameter, i, j);
							totalParameterCount++;
						}
						i = j - 1;
					}
					else {
						if (c == '?') {
							unnamedParameterCount++;
							totalParameterCount++;
						}
					}
				}
			}
			i++;
		}
		parsedSql.setNamedParameterCount(namedParameterCount);
		parsedSql.setUnnamedParameterCount(unnamedParameterCount);
		parsedSql.setTotalParameterCount(totalParameterCount);
		return parsedSql;
	}

	/**
	 * Determine whether a parameter name ends at the current position,
	 * that is, whether the given character qualifies as a separator.
	 */
	private static boolean isParameterSeparator(char c) {
		if (Character.isWhitespace(c)) {
			return true;
		}
		for (int i = 0; i < PARAMETER_SEPARATORS.length; i++) {
			if (c == PARAMETER_SEPARATORS[i]) {
				return true;
			}
		}
		return false;
	}

	public static String substituteNamedParameters(String sql, SqlParameterSource paramSource) {
		ParsedSql2 parsedSql = parseSqlStatement(sql);
		return substituteNamedParameters(parsedSql, paramSource);
	}

	@SuppressWarnings("unchecked")
	public static String substituteNamedParameters( ParsedSql2 parsedSql, SqlParameterSource paramSource) {
		String originalSql = parsedSql.getOriginalSql();
		StringBuffer actualSql = new StringBuffer();
		List<String> paramNames = parsedSql.getParameterNames();
		int lastIndex = 0;
		for (int i = 0; i < paramNames.size(); i++) {
			String paramName = (String) paramNames.get(i);
			int[] indexes = parsedSql.getParameterIndexes(i);
			int startIndex = indexes[0];
			int endIndex = indexes[1];
			actualSql.append(originalSql.substring(lastIndex, startIndex));
			Object value = paramSource.getValue(paramName);
			if (value instanceof Collection) {
				Iterator<Object> entryIter = ((Collection<Object>) value).iterator();
				int k = 0;
				while (entryIter.hasNext()) {
					if (k > 0) {
						actualSql.append(", ");
					}
					k++;
					Object entryItem = entryIter.next();
					if (entryItem instanceof Object[]) {
						Object[] expressionList = (Object[]) entryItem;
						actualSql.append("(");
						for (int m = 0; m < expressionList.length; m++) {
							if (m > 0) {
								actualSql.append(", ");
							}
							//actualSql.append("?");
							actualSql.append(expressionList[m].toString());
						}
						actualSql.append(")");
					}
					else {
						//actualSql.append("?");
						actualSql.append(entryItem.toString());
					}
				}
			}
			else {
				//actualSql.append("?");
				actualSql.append(value.toString());
			}
			lastIndex = endIndex;
		}
		actualSql.append(originalSql.substring(lastIndex, originalSql.length()));
		return actualSql.toString();
	}

}
