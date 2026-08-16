package dk.martinrohwedder.todolist_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "todolists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Todolist {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Builder.Default
    @OneToMany(mappedBy = "todolist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TodolistItem> todolistItems = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // -----------------------------------------------------------------------------------
    // Helper methods for bidirectional synchronization between Todolist and TodolistItems
    // -----------------------------------------------------------------------------------

    public void addTodolistItem(TodolistItem todolistItem) {
        todolistItems.add(todolistItem);
        todolistItem.setTodolist(this);
    }

    public void removeTodolistItem(TodolistItem todolistItem) {
        todolistItems.remove(todolistItem);
        todolistItem.setTodolist(null);
    }
}
