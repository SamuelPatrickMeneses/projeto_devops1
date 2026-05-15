window.addEventListener("load", async () => {
    let id = window.location.href.split("/").pop();

    if (isNaN(id)) {
        id = parseInt(id);
        const title = document.getElementById("title");
        const content = document.getElementById("content");
        const deleteButton = document.getElementById("delete-btn");
        deleteButton.addEventListener("click", () => {
            fetch(`http://localhost:8080/app/notes/${id}`, {
                method: "DELETE"
            }).then(() => open('/app'));
        });
        const note = await fetch("http://localhost:8080/app/notes/" + id)
            .then(res => res.json());
        title.innerText = note.title;
        content.innerText = note.content;
    } else {
        open('/app')
    }
});