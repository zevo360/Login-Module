package com.inn.zevo.login.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.inn.zevo.login.security.services.UserDetailsImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

	@Value("${jwt.token.secret.key}")
	private String secretKey;
	@Value("${jwt.token.expiration}")
	private long jwtExpiration;
	@Value("${jwt.refresh.token.expiration}")
	private long refreshExpiration;

	private final String AUDIENCE_UNKNOWN = "unknown";
	private final String AUDIENCE_WEB = "web";
	private final String AUDIENCE_MOBILE = "mobile";
	private final String AUDIENCE_TABLET = "tablet";

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	  public String generateToken(UserDetailsImpl userDetails) {
		  Map<String, Object> claims= new HashMap<>();
		  claims.put("auth", userDetails.getAuthorities().stream().filter(Objects::nonNull).collect(Collectors.toList()));
		  claims.put("email", userDetails.getEmail());
		  claims.put("audience", this.generateAudience("WEB"));
		  claims.put("sub", userDetails.getUsername());
	    return generateToken(claims, userDetails);
	  }

	public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
		return buildToken(extraClaims, userDetails, jwtExpiration);
	}

	public String generateRefreshToken(UserDetails userDetails) {
		return buildToken(new HashMap<>(), userDetails, refreshExpiration);
	}

	private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
		return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(getSignInKey(), SignatureAlgorithm.HS256).compact();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
	}

	private Key getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	private String generateAudience(String device) {
		String audience = this.AUDIENCE_UNKNOWN;
		if (device.equalsIgnoreCase(this.AUDIENCE_WEB)) {
			audience = this.AUDIENCE_WEB;
		} else if (device.equalsIgnoreCase(this.AUDIENCE_TABLET)) {
			audience = AUDIENCE_TABLET;
		} else if (device.equalsIgnoreCase(this.AUDIENCE_MOBILE)) {
			audience = AUDIENCE_MOBILE;
		}
		return audience;
	}
}

