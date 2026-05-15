window.addEventListener("load", async () => {

    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    const id = urlParams.get('id'); // "123"
    console.log(id);
    console.log("oi");
    if (!isNaN(id)) {
        const numericId = parseInt(id, 10);
        const title = document.getElementById("title");
        const content = document.getElementById("content");
        const updateButton = document.getElementById("update-btn");
        const deleteButton = document.getElementById("delete-btn");
        deleteButton.addEventListener("click", () => {
            fetch(`/api/notes/${numericId}`, {
                method: "DELETE"
            }).then(() => open('/', '_self'));
        });
        fetch("/api/notes/" + numericId)
            .then(res => res.json())
            .then((note) => {

                title.value = note.title;
                content.value = note.content;
                updateButton.addEventListener("click",  () => {
                    fetch("/api/notes/" + numericId, {
                        method: "PUT",
                        body: JSON.stringify({
                            title: title.value,
                            content: content.value
                        }),
                        headers: {
                          'Content-Type': 'application/json'
                        }
                    }).then(async (e) => {
                        console.log( await e.json());
                        open('/', '_self')
                    })
                    .catch((e) => {
                        console.error(e);
                        open('/', '_self')
                    });
                });
            });
    } else {
        open('/', '_self')
    }
});
