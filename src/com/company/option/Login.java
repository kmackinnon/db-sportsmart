import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import static com.company.Connect.openConnection;

public class Login extends Option {
	private static final String NAME = "Login";
	private static final String[] SUBOPTION_NAMES = { "username", "password" };
	private static final String QUERY = "SELECT name, password_hash, password_salt FROM Customer WHERE name=?;";

	public Login() {
		super(NAME, Arrays.asList(SUBOPTION_NAMES));
	}

	public ResultSet execute() throws SQLException {
		String username = subOptionValues.get(SUBOPTION_NAMES[0]);
		String pass = subOptionValues.get(SUBOPTION_NAMES[1]);

		try (Connection c = openConnection();
				PreparedStatement stmt = c.prepareStatement(QUERY)) {
			// look up username, hash, salt
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();

			// compare and return null if check fails
			String hash = rs.getString("password_hash");
			String salt = rs.getString("password_salt");

			// TODO: use Ze's hash/salt checker - if no match, return null

			// return username in resultset if check succeeds
			return rs;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}
