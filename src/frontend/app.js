const searchInput = document.getElementById('search-input');
const searchBtn = document.getElementById('search-btn');
const resultsList = document.getElementById('results-list');

function fetchTweets(keyword) {
    return [
        `Tweet about ${keyword} #1`,
        `Tweet about ${keyword} #2`,
        `Tweet about ${keyword} #3`
    ];
}

searchBtn.addEventListener('click', () => {
    const keyword = searchInput.value.trim();
    if (keyword === '') {
        alert('Please enter a keyword!');
        return;
    }

    const tweets = fetchTweets(keyword);

    resultsList.innerHTML = '';

    tweets.forEach(tweet => {
        const li = document.createElement('li');
        li.textContent = tweet;
        resultsList.appendChild(li);
    });
});
