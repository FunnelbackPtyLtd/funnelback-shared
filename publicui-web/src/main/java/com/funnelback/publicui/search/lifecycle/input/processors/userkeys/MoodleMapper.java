package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>
 * This class generates the credentials for a specific user of the search engine
 * on Moodle.
 * </p>
 * 
 * @author mraymond
 * 
 */
public class MoodleMapper implements UserKeysMapper {

	static private Logger logger = Logger.getLogger(MoodleMapper.class);

	/**
	 * Constant to get enrollments of a specific user
	 */
	private static final String MOODLE_PARAMETER_NAME = "username";
	/**
	 * Constant to separate the strings
	 */
	private static final String SEPARATOR = "_";
	/**
	 * The query which gets the list of courses where each user is enrolled, and
	 * his role for each course
	 */
	private static final String course = "SELECT mdl_user.username,  mdl_role.id as role,group_concat(mdl_course.id order by mdl_course.id asc) as courses FROM mdl_course INNER JOIN mdl_context ON mdl_context.instanceid = mdl_course.id INNER JOIN mdl_role_assignments ON mdl_context.id = mdl_role_assignments.contextid INNER JOIN mdl_role ON mdl_role.id = mdl_role_assignments.roleid INNER JOIN mdl_user ON mdl_user.id = mdl_role_assignments.userid group by username, role order by username ";
	/**
	 * The query which gets exception about specific role for each module
	 */
	private static final String module = "SELECT mdl_user.username,  mdl_role.id as role,group_concat(mdl_course_modules.id) as modules, mdl_course_modules.course FROM mdl_course_modules  INNER JOIN mdl_context ON mdl_context.instanceid = mdl_course_modules.id INNER JOIN mdl_role_assignments ON mdl_context.id = mdl_role_assignments.contextid INNER JOIN mdl_role ON mdl_role.id = mdl_role_assignments.roleid INNER JOIN mdl_user ON mdl_user.id = mdl_role_assignments.userid where mdl_context.contextlevel = '70' group by username, role, mdl_course_modules.course order by username";

	/**
	 * <p>
	 * Method to get the userkeys for a Moodle user.
	 * </p>
	 * <p>
	 * We need to connect the database and get the enrollments for the user.
	 * </p>
	 * 
	 * @param transaction
	 *            The way to get the configuration of the collection
	 * @return The userkeys
	 */
	public List<String> getUserKeys(SearchTransaction transaction) {

		List<String> rc = new ArrayList<String>();
		Connection connection = null;
		Statement statCourse = null;
		Statement statModule = null;

		try {

			// Get the configuration of the collection to reach the Moodle's db
			// And open database connection
			connection = this.getJdbcConnection(transaction.getQuestion()
					.getCollection().getConfiguration());

			statCourse = connection.createStatement(
					java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statModule = connection.createStatement(
					java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			// Get the userkeys
			String userkeys = "";
	
			userkeys = this.makeUserkeys(transaction.getQuestion().getInputParameterMap()
					.get(MOODLE_PARAMETER_NAME),
					statCourse.executeQuery(course),
					statModule.executeQuery(module));
			// Add userkeys as a list with one string
			rc.add(userkeys);

		} catch (Exception e) {
			logger.error("Unknown error while getting the userkeys", e);
			rc.add(MasterKeyMapper.MASTER_KEY);
		} finally {
			// Close the statements and the connection through the db
			if (connection != null) {
				this.closeConnection(connection);
			}
			if (statCourse != null) {
				this.closeStatement(statCourse);
			}
			if (statModule != null) {
				this.closeStatement(statModule);
			}
		}
		
		// Result
		return rc;
	}

	/**
	 * <p>
	 * Same method than the one used in a databse collection via the dbgather
	 * code The connection through the Moodle's db is carried out.
	 * </p>
	 * 
	 * @param configData
	 * @return The connector
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws MalformedURLException
	 */
	public final Connection getJdbcConnection(Config configData)
			throws ClassNotFoundException, SQLException, MalformedURLException {

		String jdbcClass = configData.value("db.jdbc_class");

		if (jdbcClass == null || jdbcClass.length() == 0) {
			throw new ClassNotFoundException("No db.jdbc_class in config");
		}

		Class.forName(jdbcClass);

		String jdbc_url = configData.value(Keys.Database.JDBC_ADDRESS);
		String username = configData.value(Keys.Database.JDBC_USERNAME);
		String password = configData.value(Keys.Database.JDBC_PASSWORD);

		Connection conn = DriverManager.getConnection(jdbc_url, username,
				password);
		return conn;
	}

	/**
	 * <p>
	 * Method which closes the connection with the db.
	 * </p>
	 * 
	 * @throws SQLException
	 */
	public void closeConnection(Connection connection) {
		try {
			connection.close();
		} catch (SQLException e) {
			logger.warn("Could not close connection", e);
		}
	}

	/**
	 * <p>
	 * Method which closes a statement.
	 * </p>
	 * 
	 * @throws SQLException
	 */
	public void closeStatement(Statement stat) {
		try {
			stat.close();
		} catch (SQLException e) {
			logger.warn("Could not close statement", e);
		}
	}

	/**
	 * <p>
	 * Method which produces the credentials, basing on the course query.
	 * </p>
	 * 
	 * @param username
	 *            A specific user
	 * @throws SQLException
	 */
	public String makeUserkeys(String username, ResultSet queryCourse,
			ResultSet queryModule) throws SQLException {
		String s = "";
		String rc = "";

		// Get the targeted result of the query & put it in a string
		while (queryCourse.next()) {
			if (queryCourse.getString("username").equals(username)) {
				if (queryCourse.getString("courses").contains(",")) {
					for (int i = 0; i < queryCourse.getString("courses")
							.length(); i++) {
						if (!queryCourse.getString("courses")
								.substring(i, i + 1).equals(",")) {
							s += queryCourse.getString("courses").substring(i,
									i + 1);
						} else {
							rc += "C" + s + SEPARATOR + "R"
									+ queryCourse.getString("role") + SEPARATOR;
							s = "";
						}
					}
					rc += "C" + s + SEPARATOR + "R"
							+ queryCourse.getString("role") + SEPARATOR;
				} else {
					rc += "C" + queryCourse.getString("courses") + SEPARATOR
							+ "R" + queryCourse.getString("role") + SEPARATOR;
				}
			}
		}

		s = "";

		while (queryModule.next()) {
			if (queryModule.getString("username").equals(username)) {
				if (queryModule.getString("modules").contains(",")) {
					for (int i = 0; i < queryModule.getString("modules")
							.length(); i++) {
						if (!queryModule.getString("modules")
								.substring(i, i + 1).equals(",")) {
							s += queryModule.getString("modules").substring(i,
									i + 1);
						} else {
							rc += "C" + queryModule.getString("course")
									+ SEPARATOR + "M" + s + SEPARATOR + "R"
									+ queryModule.getString("role") + SEPARATOR;
							s = "";
						}
					}
					rc += "C" + queryModule.getString("course") + SEPARATOR
							+ "M" + s + SEPARATOR + "R"
							+ queryModule.getString("role") + SEPARATOR;
				} else {
					rc += "C" + queryModule.getString("course") + SEPARATOR
							+ "M" + queryModule.getString("modules")
							+ SEPARATOR + "R" + queryModule.getString("role")
							+ SEPARATOR;
				}
			}
		}
		
		// Log display
		logger.debug("Keys for the user '" + username + "' = '" + rc + "'");
		
		return rc;
	}
}
