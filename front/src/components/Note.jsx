import React from "react";

const Note = ({note, deleteNote, editNote}) => (

    <li className="list-group-item row">
        <h2 className="h4">{note.content.title}</h2>
        <p className="">{note.content.body}</p>
        <button onClick={deleteNote} type="button" className="btn btn-sm btn-outline-danger"><i
            className="bi bi-trash"/></button>
        <button onClick={editNote} type="button" className="btn btn-sm btn-outline-info ml-2"><i
            className="bi bi-pen"/></button>
    </li>

)

export default Note;
