window.addEventListener("load", async () => {
    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    const pathId = urlParams.get('id');
    console.log(pathId);

    if (!isNaN(pathId)) {
        const numericId = parseInt(pathId, 10);
        const title = document.getElementById("title");
        const content = document.getElementById("content");
        const deleteButton = document.getElementById("delete-btn");
        const updateButton = document.getElementById("update-btn");

        updateButton.addEventListener("click", () => {
            open(`/update.html?id=${numericId}`, '_self');
        });

        deleteButton.addEventListener("click", () => {
            fetch(`/api/notes/${numericId}`, {
                method: "DELETE"
            }).then(() => open('/', '_self'));
        });

        fetch("/api/notes/" + numericId)
            .then(res => res.json())
            .then((note) => {
                title.innerText = note.title;
                content.innerText = note.content;
            })
            .catch(() => open('/', '_self'));
    } else {
        open('/', '_self');
    }
});
