import React from "react";

const Note = ({note}) => (
    <div className="col">
        <div className="card">
            <div className="card-body">
                <h5 className="card-title">{note.content.title}</h5>
                <p className="card-text">{note.content.body}</p>
            </div>
        </div>
    </div>
)

export default Note;
