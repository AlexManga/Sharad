import React from "react";

const Note = ({note, deleteNote, editNote}) => (
    <div className="col">
        <div className="card">
            <div className="card-body">
                <h5 className="card-title">{note.content.title}</h5>
                <p className="card-text">{note.content.body}</p>
                <button onClick={deleteNote} type="button" className="btn btn-sm btn-outline-danger"><i className="bi bi-trash"/></button>
                <button onClick={editNote} type="button" className="btn btn-sm btn-outline-info ml-2"><i className="bi bi-pen"/></button>
            </div>
        </div>
    </div>
)

export default Note;
