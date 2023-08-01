const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 3000;

const server = http.createServer((req, res) => {
    const filePath = req.url === '/' ? '/index.html' : req.url;
    const fullPath = path.join(__dirname, filePath);

    fs.readFile(fullPath, (err, content) => {
        if (err) {
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            res.end('File not found');
        } else {
            const extension = path.extname(fullPath);
            const contentType = getContentType(extension);
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(content);
        }
    });
});

server.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}`);
});

function getContentType(extension) {
    switch (extension) {
        case '.html':
            return 'text/html';
        case '.js':
            return 'text/javascript';
        default:
            return 'text/plain';
    }
}
