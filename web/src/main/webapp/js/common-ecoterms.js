const ecoterms = [];
const xhr = new XMLHttpRequest();
xhr.onreadystatechange = function () {
    if (xhr.readyState == 4 && xhr.status == 200) {
        const content = xhr.responseText;
        const lines = content.split('\n');
        for (let i = 1; i < lines.length; i++) {
            const fields = lines[i].split('\t');
            if (fields.length == 3) { // the correct rows
                ecoterms.push(fields);
            }
        }
    }
};
xhr.open('GET', './common_eco_terms.txt', async = false);
xhr.send();