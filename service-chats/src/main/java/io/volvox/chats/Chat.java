package io.volvox.chats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.Max;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import org.hibernate.annotations.SortNatural;

@Entity
@Cacheable
public class Chat extends PanacheEntityBase {

    @Id
    @Positive(message = "id is not positive")
    @Max(message = "id is too big", value = ChatId.MASK)
    @Column(nullable = false, unique = true)
	@JsonSerialize(using = ChatIdLongJsonSerializer.class)
	@JsonDeserialize(using = ChatIdLongJsonDeserializer.class)
    public Long id;

	/**
	 * null = unknown username
	 * "" = empty username
	 */
    @Column(length = 128)
    public String name;

	// Field definition and bounds
	@OneToMany(orphanRemoval = true, mappedBy = "chat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderBy("time DESC")
	@SortNatural
	// Field serialization
	@JsonIgnore
	public SortedSet<HistoricChatName> nameHistory = new TreeSet<>();

	/**
	 * null = unknown username
	 * "" = empty username
	 */
    @Column(length = 48)
    @Pattern(message = "Username contains invalid characters", regexp = "^(?:|(?:[a-zA-Z\\d][_]?)+)$")
    @Pattern(message = "Username is not valid", regexp = "^(?:|(?:translation_|mv_)?[a-zA-Z]([a-zA-Z_\\d]){1,30}[a-zA-Z\\d])$")
    public String username;

	// Field definition and bounds
	@OneToMany(orphanRemoval = true, mappedBy = "chat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderBy("time DESC")
	@SortNatural
	// Field serialization
	@JsonIgnore
	public SortedSet<HistoricChatUsername> usernameHistory = new TreeSet<>();

    @Enumerated
    public Status status;


	@JsonIgnore
    public ChatId getChatId() {
        return ChatId.fromLong(id);
    }

    @JsonIgnore
    public void setChatId(ChatId id) {
        this.id = id.toLong();
    }

    @Override public String toString() {
        return new StringJoiner(", ", Chat.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("username='" + username + "'")
                .add("status=" + status)
                .toString();
    }

	public static Uni<Chat> findUsername(String username) {
		if (username == null) {
			throw new NullPointerException("Username must not be null");
		} else if (username.isBlank()) {
			throw new NullPointerException("Username must not be blank");
		}
		return find("from Chat where username = ?1", username).firstResult();
	}
}
