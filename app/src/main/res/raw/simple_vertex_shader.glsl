attribute vec2 inputTextureCoordinate;
attribute vec4 a_Position;
varying   vec2 textureCoordinate;

 void main()
 {
     gl_Position = a_Position;
     textureCoordinate = inputTextureCoordinate;
 }
