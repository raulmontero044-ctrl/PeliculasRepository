package org.example.auth.persistence.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.example.auth.domain.AuthToken;
import org.example.auth.domain.TokenTipo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenRowMapper implements RowMapper<AuthToken> {

    @Override
    public AuthToken mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuthToken token = new AuthToken();
        token.setId(rs.getLong("id"));
        token.setToken(rs.getString("token"));
        token.setUserId(rs.getLong("user_id"));
        token.setTipo(TokenTipo.valueOf(rs.getString("tipo")));
        token.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
        token.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return token;
    }
}
