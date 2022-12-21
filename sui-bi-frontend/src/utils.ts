import React from "react";
import ReactDOM from "react-dom";

export function draw(element: React.ReactElement, containerRef?: React.RefObject<any>): void {
  const elementContainer = containerRef && containerRef.current
    ? ReactDOM.findDOMNode(containerRef.current)
    : document.createElement("div");

  ReactDOM.render(element as any, elementContainer as any);
}
