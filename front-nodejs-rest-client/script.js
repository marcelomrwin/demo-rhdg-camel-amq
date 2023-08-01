const API_BASE = 'http://localhost:8089';

async function fetchData() {
    try {
        const apiUrl = API_BASE + '/api/' + $("#cacheId").val();
        const response = await fetch(apiUrl);
        const result = await response.json();

        if (result.type === 'LOAD') {
            $("#messages").html(result.message);
            $("#result").empty();
        } else if (result.type === 'DATA') {
            $("#result").html(JSON.stringify(result.registry, null, 2));
            hljs.highlightElement(document.getElementById("result"));
            $("#messages").html(result.message);
        }
    } catch (error) {
        console.error('Error fetching data:', error);
    }
}