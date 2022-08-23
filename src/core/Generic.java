/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author ´Gabriel
 */
public class Generic<T> {
    private Class<T> classe;

    public Generic(Class<T> classe) {
        this.classe = classe;
    }

    public T buildOne() throws InstantiationException, IllegalAccessException {
        return classe.newInstance();
    }
}
