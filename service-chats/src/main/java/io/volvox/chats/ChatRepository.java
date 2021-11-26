package io.volvox.chats;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

@ApplicationScoped
@NamedQueries({
        @NamedQuery(name = "Chat.getByName", query = "from Chat where name = ?1"),
        @NamedQuery(name = "Chat.getByUsername", query = "from Chat where username = ?1"),
        @NamedQuery(name = "Chat.countByStatus", query = "select count(*) from Chat p where p.status = :status"),
        @NamedQuery(name = "Chat.updateStatusById", query = "update Chat p set p.status = :status where p.id = :id"),
        @NamedQuery(name = "Chat.deleteById", query = "delete from Chat p where p.id = ?1")
})
public class ChatRepository implements PanacheRepositoryBase<Chat, Long> {

    public Uni<Chat> findByUsername(String username) {
        return find("#Chat.getByUsername", username).firstResult();
    }
}
