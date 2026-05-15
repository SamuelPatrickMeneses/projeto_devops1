window.addEventListener("load", async () => {

    const title = document.getElementById("title");
    const content = document.getElementById("content");
    const saveButton = document.getElementById("save-btn");
    saveButton.addEventListener("click", () => {
        fetch("/api/notes/", {
            method: "POST",
            body: JSON.stringify({
                title: title.value,
                content: content.value
            }),
            headers: {
              'Content-Type': 'application/json'
            }
        }).then(() => {
            open('/', '_self')
        })
        .catch(console.log);
    });
});
