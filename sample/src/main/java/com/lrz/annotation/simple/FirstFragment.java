package com.lrz.annotation.simple;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.lrz.annotation.simple.data.TextConfig;
import com.lrz.annotation.simple.databinding.FragmentFirstBinding;
import com.lrz.multi.MultiData;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    TextConfig config;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        binding.b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                config = new TextConfig();
                config.setBookId("3221");
                DataSimple simple = MultiData.DATA.get(DataSimple.class);
                simple.getStr();
                simple.setStr("my name");
                MultiData.DATA.clear(Data2Simple.class);
                MultiData.DATA.get(Data2Simple.class).setConfig1(config);
                System.out.println("--------1read:" + MultiData.DATA.get(Data2Simple.class).getConfig1());
            }
        });

        binding.b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MultiData.DATA.get(CollectionData.class).getMap().put("123","1");
            }
        });
        binding.b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("--------read:" + MultiData.DATA.get(CollectionData.class));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}