const sseSource = new EventSource("http://localhost:9000/api/events/create?diff=true");

sseSource.onmessage = function (event) {
    const { t } = JSON.parse(event.data);
    console.log(t);
}
