const deleteButton = document.getElementById('delete-btn');

if (deleteButton) {
    deleteButton.addEventListener('click', event => {
        let id = document.getElementById('article-id').value;
        fetch(`/api/articles/${id}`, {
            method: 'DELETE'
        })
            .then(() => {
                alert('삭제가 완료되었습니다.');
                location.replace('/articles');
            });
    });
}


const modifyButton = document.getElementById('modify-btn');

if (modifyButton) {
    modifyButton.addEventListener('click', event => {
        let params = new URLSearchParams(location.search);
        let id = params.get('id');

        fetch(`/api/articles/${id}`, {
            method: 'PUT',
            headers: {
                "Content-Type": "application/json;",
            },
            body: JSON.stringify({
                title: document.getElementById('title').value,
                content: document.getElementById('content').value,
            })
        })
            .then(()=>{
                alert('수정이 완료되었습니다.');
                location.replace('/articles/${id}');
            })
    });
}


const createButton = document.getElementById('create-btn');

if (createButton) {
    createButton.addEventListener('click', async (event) => {
        event.preventDefault();

        const title = document.getElementById('title').value;
        const content = document.getElementById('content').value;

        try {
            const response = await fetch('/api/articles', {
                method: 'POST',
                headers: {
                    "Content-Type": "application/json;",
                },
                body: JSON.stringify({ title, content }),
            });

            if (!response.ok) {
                let message = "요청이 실패했습니다.";
                try {
                    const contentType = response.headers.get('Content-Type') || '';
                    if (contentType.includes('application/json')) {
                        const data = await response.json();
                        message = data?.message || data?.error || message;
                    } else {
                        const text = await response.text();
                        message = text || `${response.status} ${response.statusText}`;
                    }
                } catch {

                }
                throw new Error(message);
            }
            alert('등록 완료되었습니다.');
            // 성공 시 이동
            location.replace('/articles');
        } catch(err) {
            console.error(err);
            alert(`등록 실패: ${err?.message || '알 수 없는 오류가 발생했습니다.'}`);
            // 실패 시에도 이동
            location.replace('/articles');

        }
    });
}


// 삭제 기능
const deleteButton = document.getElementById("delete-btn");

if (deleteButton) {
    deleteButton.addEventListener("click", (event) => {
        let id = document.getElementById("article-id").value;
        function success() {
            alert("삭제가 완료되었습니다.");
            location.replace("/articles");
        }

        function fail() {
            alert("삭제 실패했습니다.");
            location.replace("/articles");
        }

        httpRequest("DELETE", `/api/articles/${id}`, null, success, fail);
    })
}

// 수정 기능
const modifyButton = document.getElementById("modify-btn");

if (modifyButton) {
    modifyButton.addEventListener("click", (event) => {
        let params = new URLSearchParams(location.search);
        let id = params.get("id");

        body = JSON.stringify({
            title: document.getElementById("title").value,
            content: document.getElementById("content").value,
        });

        function success() {
            alert("수정 완료되었습니다.");
            location.replace(`/articles/${id}`);
        }

        function fail() {
            alert("수정 실패했습니다.")
            location.replace(`/articles/${id}`);
        }

        httpRequest("PUT", `/api/articles/${id}`, body, success, fail);
    })
}

// 쿠키를 가져오는 함수
function getCookie(key) {
    var result = null;
    var cookies = document.cookie.split(",");
    cookie.some(function (item) {
        item = item.replace(" ", "");

        var dic = item.split("=");

        if (key === dic[0]) {
            result = dic[1];
            return true;
        }
    });

    return result;
}

// HTTP 요청을 보내는 함수
function httpRequest(method, url, body, success, fail) {
    fetch(url, {
        method: method,
        headers: {
            // 로컬 스토리지에서 엑세스 토큰 값ㅇ르 가져와 헤더에 추가
            Authorization: "Bearer " + localStorage.getItem("access_token"),
            "Content-Type": "application/json;",
        },
        body: body,
    }).then((response) => {
        if (response.status === 200 || response.status === 201) {
            return success();
        }
        const refresh_token = getCookie("refresh_token");
        if (response.status === 401 && refresh_token) {
            fetch("/api/token", {
                method: "POST",
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("access_token"),
                    "Content-Type": "application/json;",
                },
                body: JSON.stringify({
                    refreshToken: getCookie("refresh_token"),
                }),
            })
                .then((res) => {
                    if (res.ok) {
                        return res.json();
                    }
                })
                .then((result) => {
                    // 재발급이 성공하면 로컬 스토리지값을 새로운 엑세스 토큰으로 교체
                    localStorage.setItem("access_token", result.accessToken);
                    httpRequest(method, url, body, success, fail);
                })
                .catch((error) => fail());
        } else {
            return fail();
        }
    })
}