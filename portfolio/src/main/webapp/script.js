// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random fact about me to the page.
 */
function addRandomFact() {
  const facts =
    ["I'm a first generation student.", 
    "My favorite color is orange.", 
    "My favorite song at the moment is Darkness by Sweet Trip.", 
    "I'm trying to play tetris competitively.",
    "I'm currently learning Apache Kafka.",
    "My favourite season is the period between summer and autumn.",
    "I added an easter egg to my site; can you figure it out?"
    ];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/**
 * hackerman 
 */
let secretPosition = 0;
const SECRET_CODE = [38, 38, 40, 40, 37, 39, 37, 39, 66, 65];
document.addEventListener('keydown', (e) => {
  secretPosition = (e.keyCode == SECRET_CODE[secretPosition]) ? secretPosition + 1 : 0;
  if (secretPosition == SECRET_CODE.length) {
    activateSecret();
  }
});

function activateSecret() {
  document.body.style.backgroundImage = "url('public/images/secret.gif')";
  var audio = new Audio('public/secret.mp3');
  audio.play();
}

/**
 * Fetches comments from the server and adds it to the DOM.
 */
 async function getComments() {
  const response = await fetch('/comments');
  const comments = await response.json();
  
  const commentsListElement = document.getElementById('comment-container');
  commentsListElement.innerHTML = '';
  for (let i = 0; i < comments.length; i++) {
    comment = comments[i];
    formattedComment = `${comment.strDate} ${comment.commenterName} says: "${comment.text}"`;
    commentsListElement.appendChild(
      createListElement(formattedComment)
    );

    if (comment.hasOwnProperty("imageUrl")){
      const commentImage = document.createElement("img");
      commentImage.src = comment.imageUrl;
      commentsListElement.appendChild(commentImage);
    }
  }
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

function fetchBlobstoreUrl() {
  fetch('/blobstore-upload-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        const messageForm = document.getElementById('comment-form');
        messageForm.action = imageUploadUrl;
      });
}