window.addEventListener("load", async () => {
    const notes = await fetch("/api/notes").then(res => res.json());
    const noteContainer = document.getElementById("notes");
    console.log(notes);
    if (notes.length === 0) {
        noteContainer.innerHTML = `
            <p>Sem notas.</p>
        `;
    } else {
        notes.forEach(note => {
            const noteElement = document.createElement("div");
            noteElement.innerHTML = `
                <h3>${note.title}</h3>
                <p>${note.content}</p>
                <a href="update.html?id=${note.id}"> Editar</a>
                <button class="delete-btn" data-id="${note.id}">Deletar</button>
            `;
            const deleteButton = noteElement.querySelector('.delete-btn');
            deleteButton.addEventListener("click", () => {
                fetch(`/api/notes/${note.id}`, {
                    method: "DELETE"
                }).then(() => location.reload());
            });
            noteContainer.appendChild(noteElement);
        });
    }
});
